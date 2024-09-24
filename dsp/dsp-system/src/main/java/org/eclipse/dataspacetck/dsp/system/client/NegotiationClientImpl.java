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

package org.eclipse.dataspacetck.dsp.system.client;

import okhttp3.Response;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.client.NegotiationClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;

import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.getJson;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.compactStringProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createDspContext;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createNegotiationResponse;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringIdProperty;

/**
 * Default implementation that supports dispatch to a local, in-memory test connector or a remote connector system via HTTP.
 */
public class NegotiationClientImpl implements NegotiationClient {
    private static final String GET_PATH = "negotiations/%s";
    private static final String REQUEST_PATH = "negotiations/request";
    private static final String TERMINATE_PATH = "negotiations/%s/termination";
    private static final String EVENT_PATH = "negotiations/%s/events";
    private static final String VERIFICATION_PATH = "negotiations/%s/agreement/verification";
    private String connectorBaseUrl;
    private Connector systemConnector;
    private Monitor monitor;

    public NegotiationClientImpl(String connectorBaseUrl, Monitor monitor) {
        this.connectorBaseUrl = connectorBaseUrl.endsWith("/") ? connectorBaseUrl : connectorBaseUrl + "/";
        this.monitor = monitor;
    }

    public NegotiationClientImpl(Connector systemConnector, Monitor monitor) {
        this.systemConnector = systemConnector;
        this.monitor = monitor;
    }

    @Override
    public Map<String, Object> contractRequest(Map<String, Object> contractRequest, boolean expectError) {
        if (systemConnector != null) {
            try {
                var compacted = processJsonLd(contractRequest, createDspContext());
                var negotiation = systemConnector.getProviderNegotiationManager().handleContractRequest(compacted);
                if (expectError) {
                    throw new AssertionError("Expected to throw an error on termination");
                }
                return processJsonLd(createNegotiationResponse(negotiation.getId(),
                        negotiation.getCorrelationId(),
                        negotiation.getState().toString().toLowerCase()), createDspContext());
            } catch (IllegalStateException e) {
                // if the error is expected, swallow exception
                if (!expectError) {
                    throw e;
                }
                return Map.of();
            }
        } else {
            try (var response = postJson(connectorBaseUrl + REQUEST_PATH, contractRequest, expectError)) {
                monitor.debug("Received contract request response");
                //noinspection DataFlowIssue
                return processJsonLd(response.body().byteStream(), createDspContext());
            }
        }
    }

    @Override
    public void terminate(Map<String, Object> termination, boolean expectError) {
        if (systemConnector != null) {
            var compacted = processJsonLd(termination, createDspContext());
            try {
                systemConnector.getProviderNegotiationManager().terminate(compacted);
                if (expectError) {
                    throw new AssertionError("Expected to throw an error on termination");
                }
            } catch (IllegalStateException e) {
                // if the error is expected, swallow exception
                if (!expectError) {
                    throw e;
                }
            }
        } else {
            var providerId = compactStringProperty(DSPACE_PROPERTY_PROVIDER_PID, termination);
            try (var response = postJson(connectorBaseUrl + format(TERMINATE_PATH, providerId), termination, expectError)) {
                validateResponse(response, providerId, expectError, "terminate");
                monitor.debug("Received negotiation terminate response: " + providerId);
            }
        }
    }

    @Override
    public Map<String, Object> getNegotiation(String providerPid) {
        if (systemConnector != null) {
            var negotiation = systemConnector.getProviderNegotiationManager().findById(providerPid);
            var consumerPid = negotiation.getCorrelationId();
            var state = DSPACE_NAMESPACE + negotiation.getState().toString();
            return processJsonLd(createNegotiationResponse(providerPid, consumerPid, state), createDspContext());
        } else {
            try (var response = getJson(connectorBaseUrl + format(GET_PATH, providerPid))) {
                //noinspection DataFlowIssue
                var jsonResponse = processJsonLd(response.body().byteStream(), createDspContext());
                var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, jsonResponse); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
                var state = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, jsonResponse); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
                monitor.debug(format("Received negotiation status response with state %s: %s", state, providerId));
                return jsonResponse;
            }
        }
    }

    @Override
    public void consumerAccept(Map<String, Object> event) {
        if (systemConnector != null) {
            var compacted = processJsonLd(event, createDspContext());
            var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, compacted); // // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
            systemConnector.getProviderNegotiationManager().handleConsumerAgreed(providerId);
        } else {
            var providerId = compactStringProperty(DSPACE_PROPERTY_PROVIDER_PID, event);
            try (var response = postJson(connectorBaseUrl + format(EVENT_PATH, providerId), event)) {
                if (!response.isSuccessful()) {
                    throw new AssertionError(format("Accept event failed with code %s: %s ", response.code(), providerId));
                }
                monitor.debug("Received accept response: " + providerId);
            }

        }
    }

    @Override
    public void consumerVerify(Map<String, Object> verification, boolean expectError) {
        if (systemConnector != null) {
            var compacted = processJsonLd(verification, createDspContext());
            var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, compacted); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
            try {
                systemConnector.getProviderNegotiationManager().handleConsumerVerified(providerId, verification);
                if (expectError) {
                    throw new AssertionError("Expected to throw an error on termination");
                }
            } catch (IllegalStateException e) {
                // if the error is expected, swallow exception
                if (!expectError) {
                    throw e;
                }
            }
        } else {
            var providerId = compactStringProperty(DSPACE_PROPERTY_PROVIDER_PID, verification);
            try (var response = postJson(connectorBaseUrl + format(VERIFICATION_PATH, providerId), verification, expectError)) {
                validateResponse(response, providerId, expectError, "verify");
                monitor.debug("Received verification response: " + providerId);
            }
        }
    }

    private void validateResponse(Response response, String providerId, boolean expectError, String type) {
        if (expectError) {
            if (response.isSuccessful()) {
                throw new AssertionError(format("Invalid %s did not fail: %s", type, providerId));
            }
        } else {
            if (!response.isSuccessful()) {
                throw new AssertionError(format("Request %s failed with code %s: %s", type, response.code(), providerId));
            }
        }
    }
}
