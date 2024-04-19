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

import org.eclipse.dataspacetck.dsp.system.api.client.NegotiationClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;

import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.getJson;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createNegotiationResponse;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringProperty;

/**
 * Default implementation that supports dispatch to a local, in-memory test connector or a remote connector system via HTTP.
 */
public class NegotiationClientImpl implements NegotiationClient {
    private static final String GET_PATH = "negotiations/%s";
    private static final String REQUEST_PATH = "negotiations/request";
    private static final String TERMINATE_PATH = "negotiations/%s/termination";
    private String connectorBaseUrl;
    private Connector systemConnector;

    public NegotiationClientImpl(String connectorBaseUrl) {
        this.connectorBaseUrl = connectorBaseUrl.endsWith("/") ? connectorBaseUrl : connectorBaseUrl + "/";
    }

    public NegotiationClientImpl(Connector systemConnector) {
        this.systemConnector = systemConnector;
    }

    @Override
    public Map<String, Object> contractRequest(Map<String, Object> contractRequest) {
        if (systemConnector != null) {
            var compacted = processJsonLd(contractRequest);
            var negotiation = systemConnector.getProviderNegotiationManager().handleContractRequest(compacted);
            return processJsonLd(createNegotiationResponse(negotiation.getId(),
                    negotiation.getCorrelationId(),
                    negotiation.getState().toString().toLowerCase()));
        } else {
            try (var response = postJson(connectorBaseUrl + REQUEST_PATH, contractRequest)) {
                //noinspection DataFlowIssue
                return processJsonLd(response.body().byteStream());
            }
        }
    }

    @Override
    public void terminate(Map<String, Object> termination) {
        if (systemConnector != null) {
            var compacted = processJsonLd(termination);
            systemConnector.getProviderNegotiationManager().terminate(compacted);
        } else {
            var processId = stringProperty(DSPACE_PROPERTY_PROVIDER_PID, termination);
            try (var response = postJson(connectorBaseUrl + format(TERMINATE_PATH, processId), termination)) {
                if (!response.isSuccessful()) {
                    throw new AssertionError("Terminate request failed with code: " + response.code());
                }
            }
        }
    }

    @Override
    public Map<String, Object> getNegotiation(String providerPid) {
        if (systemConnector != null) {
            var negotiation = systemConnector.getProviderNegotiationManager().findById(providerPid);
            return processJsonLd(createNegotiationResponse(negotiation.getId(),
                    negotiation.getCorrelationId(),
                    negotiation.getState().toString()));
        } else {
            try (var response = getJson(connectorBaseUrl + format(GET_PATH, providerPid))) {
                //noinspection DataFlowIssue
                return processJsonLd(response.body().byteStream());
            }
        }
    }

    @Override
    public void consumerAgree(Map<String, Object> event) {
        if (systemConnector != null) {
            var compacted = processJsonLd(event);
            var processId = stringProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, compacted);
            systemConnector.getProviderNegotiationManager().handleConsumerAgreed(processId);
        }
        // TODO implement HTTP invoke
    }

    @Override
    public void consumerVerify(Map<String, Object> verification) {
        if (systemConnector != null) {
            var compacted = processJsonLd(verification);
            var processId = stringProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, compacted);
            systemConnector.getProviderNegotiationManager().handleConsumerVerified(processId, verification);
        }
        // TODO implement HTTP invoke
    }


}
