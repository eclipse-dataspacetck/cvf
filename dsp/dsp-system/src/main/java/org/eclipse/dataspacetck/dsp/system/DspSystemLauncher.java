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
import org.eclipse.dataspacetck.dsp.system.client.ConsumerNegotiationClient;
import org.eclipse.dataspacetck.dsp.system.client.ProviderNegotiationClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.Consumer;
import org.eclipse.dataspacetck.dsp.system.api.mock.ConsumerNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.api.mock.ProviderNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.ConsumerNegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.ProviderNegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.client.ConsumerNegotiationClientImpl;
import org.eclipse.dataspacetck.dsp.system.client.ProviderNegotiationClientImpl;
import org.eclipse.dataspacetck.dsp.system.connector.TckConnector;
import org.eclipse.dataspacetck.dsp.system.mock.ConsumerNegotiationMockImpl;
import org.eclipse.dataspacetck.dsp.system.mock.NoOpConsumerNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.mock.NoOpProviderNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.mock.ProviderNegotiationMockImpl;
import org.eclipse.dataspacetck.dsp.system.pipeline.ConsumerNegotiationPipelineImpl;
import org.eclipse.dataspacetck.dsp.system.pipeline.ProviderNegotiationPipelineImpl;
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

    private Map<String, Connector> consumerConnectors = new ConcurrentHashMap<>();
    private Map<String, Connector> providerConnectors = new ConcurrentHashMap<>();

    private Map<String, ProviderNegotiationMock> negotiationMocks = new ConcurrentHashMap<>();
    private Map<String, ProviderNegotiationClient> negotiationClients = new ConcurrentHashMap<>();

    private Map<String, ConsumerNegotiationMock> consumerNegotiationMocks = new ConcurrentHashMap<>();
    private Map<String, ConsumerNegotiationClient> consumerNegotiationClients = new ConcurrentHashMap<>();

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
        return type.equals(ProviderNegotiationClient.class) ||
                type.equals(Connector.class) ||
                type.equals(ProviderNegotiationMock.class) ||
                type.equals(ConsumerNegotiationMock.class) ||
                type.equals(ConsumerNegotiationPipeline.class) ||
                type.equals(ProviderNegotiationPipeline.class);
    }

    @Nullable
    @Override
    public <T> T getService(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        if (ProviderNegotiationPipeline.class.equals(type)) {
            return createProviderPipeline(type, configuration, resolver);
        } else if (ConsumerNegotiationPipeline.class.equals(type)) {
            return createConsumerPipeline(type, configuration, resolver);
        } else if (Connector.class.equals(type)) {
            return createConnector(type, configuration);
        } else if (ProviderNegotiationMock.class.equals(type)) {
            return createProviderNegotiationMock(type, configuration.getScopeId());
        } else if (ConsumerNegotiationMock.class.equals(type)) {
            return createConsumerNegotiationMock(type, configuration.getScopeId(), configuration, resolver);
        } else if (ProviderNegotiationClient.class.equals(type)) {
            return type.cast(createNegotiationClient(configuration.getScopeId()));
        } else if (ConsumerNegotiationClient.class.equals(type)) {
            return type.cast(createConsumerNegotiationClient(configuration.getScopeId(), configuration, resolver));
        }
        return null;
    }

    private <T> T createProviderPipeline(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        var scopeId = configuration.getScopeId();
        var negotiationClient = createNegotiationClient(scopeId);
        var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
        var consumerConnector = consumerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor));
        var pipeline = new ProviderNegotiationPipelineImpl(negotiationClient, callbackEndpoint, consumerConnector, monitor, waitTime);
        return type.cast(pipeline);
    }

    private <T> T createConsumerPipeline(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        var scopeId = configuration.getScopeId();
        var negotiationClient = createConsumerNegotiationClient(scopeId, configuration, resolver);
        var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
        var providerConnector = providerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor));
        var pipeline = new ConsumerNegotiationPipelineImpl(negotiationClient, callbackEndpoint, providerConnector, monitor, waitTime);
        return type.cast(pipeline);
    }

    private <T> T createProviderNegotiationMock(Class<T> type, String scopeId) {
        return type.cast(negotiationMocks.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                var connector = providerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor));
                return new ProviderNegotiationMockImpl(connector.getProviderNegotiationManager(), executor);
            } else {
                return new NoOpProviderNegotiationMock();
            }
        }));
    }

    private <T> T createConsumerNegotiationMock(Class<T> type, String scopeId, ServiceConfiguration configuration, ServiceResolver resolver) {
        return type.cast(consumerNegotiationMocks.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                var connector = consumerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor));
                var negotiationManager = connector.getConsumerNegotiationManager();
                var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
                @SuppressWarnings("DataFlowIssue") var address = callbackEndpoint.getAddress();
                return new ConsumerNegotiationMockImpl(negotiationManager, executor, address);
            } else {
                return new NoOpConsumerNegotiationMock();
            }
        }));
    }

    private <T> T createConnector(Class<T> type, ServiceConfiguration configuration) {
        var scopeId = configuration.getScopeId();
        if (configuration.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(Consumer.class))) {
            return type.cast(consumerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor)));
        }
        return type.cast(providerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor)));
    }

    private ProviderNegotiationClient createNegotiationClient(String scopeId) {
        return negotiationClients.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                return new ProviderNegotiationClientImpl(providerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor)), monitor);
            }
            return new ProviderNegotiationClientImpl(baseConnectorUrl, monitor);
        });
    }

    private ConsumerNegotiationClient createConsumerNegotiationClient(String scopeId,
                                                                      ServiceConfiguration configuration,
                                                                      ServiceResolver resolver) {
        var providerConnector = providerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor));
        return consumerNegotiationClients.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                var consumerConnector = consumerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor));
                var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
                //noinspection DataFlowIssue
                return new ConsumerNegotiationClientImpl(consumerConnector, callbackEndpoint.getAddress(), providerConnector, monitor);
            }
            return new ConsumerNegotiationClientImpl(baseConnectorUrl, providerConnector, monitor);
        });
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

}
