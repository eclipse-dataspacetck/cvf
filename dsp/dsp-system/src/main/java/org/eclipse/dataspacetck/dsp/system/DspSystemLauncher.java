/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 *
 */

package org.eclipse.dataspacetck.dsp.system;

import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.core.spi.system.ServiceConfiguration;
import org.eclipse.dataspacetck.core.spi.system.ServiceResolver;
import org.eclipse.dataspacetck.core.spi.system.SystemConfiguration;
import org.eclipse.dataspacetck.core.spi.system.SystemLauncher;
import org.eclipse.dataspacetck.dsp.system.api.client.NegotiationClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.Consumer;
import org.eclipse.dataspacetck.dsp.system.api.mock.NegotiationProviderMock;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.NegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.client.NegotiationClientImpl;
import org.eclipse.dataspacetck.dsp.system.mock.NegotiationProviderMockImpl;
import org.eclipse.dataspacetck.dsp.system.mock.NoOpNegotiationProviderMock;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.eclipse.dataspacetck.core.api.system.SystemsConstants.TCK_PREFIX;

/**
 * Instantiates and bootstraps a DSP test fixture.
 */
@SuppressWarnings("unused")
public class DspSystemLauncher implements SystemLauncher {
    private static final String LOCAL_CONNECTOR_CONFIG = TCK_PREFIX + ".dsp.local.connector";
    private static final String CONNECTOR_BASE_URL_CONFIG = TCK_PREFIX + ".dsp.connector.http.url";
    private static final String THREAD_POOL_CONFIG = TCK_PREFIX + ".dsp.thread.pool";
    private static final String DEFAULT_WAIT_CONFIG = TCK_PREFIX + ".dsp.default.wait";
    private static final int DEFAULT_WAIT_SECONDS = 15;

    private Monitor monitor;
    private ExecutorService executor;
    private String baseConnectorUrl;
    private boolean useLocalConnector;
    private long waitTime = DEFAULT_WAIT_SECONDS;

    private Map<String, Connector> clientConnectors = new ConcurrentHashMap<>();
    private Map<String, Connector> providerConnectors = new ConcurrentHashMap<>();

    private Map<String, NegotiationProviderMock> negotiationMocks = new ConcurrentHashMap<>();
    private Map<String, NegotiationClient> negotiationClients = new ConcurrentHashMap<>();

    @Override
    public void start(SystemConfiguration configuration) {
        this.monitor = configuration.getMonitor();
        waitTime = configuration.getPropertyAsLong(DEFAULT_WAIT_CONFIG, DEFAULT_WAIT_SECONDS);
        executor = newFixedThreadPool(configuration.getPropertyAsInt(THREAD_POOL_CONFIG, 10));
        useLocalConnector = configuration.getPropertyAsBoolean(LOCAL_CONNECTOR_CONFIG, false);
        if (!useLocalConnector) {
            baseConnectorUrl = configuration.getPropertyAsString(CONNECTOR_BASE_URL_CONFIG, null);
            if (baseConnectorUrl == null) {
                throw new RuntimeException("Required configuration not set: " + CONNECTOR_BASE_URL_CONFIG);
            }
        }
    }

    @Override
    public <T> boolean providesService(Class<T> type) {
        return type.equals(NegotiationClient.class) ||
                type.equals(Connector.class) ||
                type.equals(NegotiationProviderMock.class) ||
                type.equals(NegotiationPipeline.class);
    }

    @Nullable
    @Override
    public <T> T getService(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        if (NegotiationPipeline.class.equals(type)) {
            return createPipeline(type, configuration, resolver);
        } else if (Connector.class.equals(type)) {
            return createConnector(type, configuration);
        } else if (NegotiationProviderMock.class.equals(type)) {
            return createNegotiationMock(type, configuration.getScopeId());
        } else if (NegotiationClient.class.equals(type)) {
            return type.cast(createNegotiationClient(configuration.getScopeId()));
        }
        return null;
    }

    private <T> T createPipeline(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        var scopeId = configuration.getScopeId();
        var negotiationClient = createNegotiationClient(scopeId);
        var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
        var consumerConnector = clientConnectors.computeIfAbsent(scopeId, k -> new Connector(monitor));
        var pipeline = new NegotiationPipeline(negotiationClient, callbackEndpoint, consumerConnector, monitor, waitTime);
        return type.cast(pipeline);
    }

    private <T> T createNegotiationMock(Class<T> type, String scopeId) {
        return type.cast(negotiationMocks.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                var connector = providerConnectors.computeIfAbsent(scopeId, k2 -> new Connector(monitor));
                return new NegotiationProviderMockImpl(connector.getProviderNegotiationManager(), executor);
            } else {
                return new NoOpNegotiationProviderMock();
            }
        }));
    }

    private <T> T createConnector(Class<T> type, ServiceConfiguration configuration) {
        var scopeId = configuration.getScopeId();
        if (configuration.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(Consumer.class))) {
            return type.cast(clientConnectors.computeIfAbsent(scopeId, k -> new Connector(monitor)));
        }
        return type.cast(providerConnectors.computeIfAbsent(scopeId, k -> new Connector(monitor)));
    }

    private NegotiationClient createNegotiationClient(String scopeId) {
        return negotiationClients.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                return new NegotiationClientImpl(providerConnectors.computeIfAbsent(scopeId, k2 -> new Connector(monitor)), monitor);
            }
            return new NegotiationClientImpl(baseConnectorUrl, monitor);
        });
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

}
