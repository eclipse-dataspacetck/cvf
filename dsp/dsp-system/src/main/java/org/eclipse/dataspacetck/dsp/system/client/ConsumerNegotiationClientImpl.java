/*
 *  Copyright (c) 2024 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.dsp.system.client;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;

import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.getJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createContractRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createDspContext;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createNegotiationResponse;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringIdProperty;

/**
 * Default implementation that supports dispatch to a local, in-memory test connector or a remote connector system via HTTP.
 */
public class ConsumerNegotiationClientImpl implements ConsumerNegotiationClient {
    private static final String GET_PATH = "negotiations/%s";

    private String consumerConnectorBaseUrl;
    private Connector systemConsumerConnector;
    private String callbackAddress;
    private Connector providerConnector;
    private Monitor monitor;

    public ConsumerNegotiationClientImpl(String consumerConnectorBaseUrl, Connector providerConnector, Monitor monitor) {
        this.consumerConnectorBaseUrl = consumerConnectorBaseUrl.endsWith("/") ? consumerConnectorBaseUrl : consumerConnectorBaseUrl + "/";
        this.providerConnector = providerConnector;
        this.monitor = monitor;
    }

    public ConsumerNegotiationClientImpl(Connector consumerConnector,
                                         String callbackAddress,
                                         Connector providerConnector,
                                         Monitor monitor) {
        this.systemConsumerConnector = consumerConnector;
        this.callbackAddress = callbackAddress;
        this.providerConnector = providerConnector;
        this.monitor = monitor;
    }

    @Override
    public void initiateRequest(String datasetId, String offerId) {
        if (systemConsumerConnector != null) {
            var negotiation = systemConsumerConnector.getConsumerNegotiationManager().createNegotiation(datasetId, offerId);

            var contractRequest = createContractRequest(negotiation.getId(), offerId, datasetId, callbackAddress);
            var compacted = processJsonLd(contractRequest, createDspContext());
            var providerNegotiation = providerConnector.getProviderNegotiationManager().handleContractRequest(compacted);

            var correlationId = providerNegotiation.getId();
            systemConsumerConnector.getConsumerNegotiationManager().contractRequested(negotiation.getId(), correlationId);
        }
    }

    @Override
    public void contractOffer(Map<String, Object> offer, boolean expectError) {
        var compacted = processJsonLd(offer, createDspContext());
        if (systemConsumerConnector != null) {
            systemConsumerConnector.getConsumerNegotiationManager().handleOffer(compacted);
        }
    }

    @Override
    public void contractAgreement(Map<String, Object> agreement) {
        var compacted = processJsonLd(agreement, createDspContext());
        if (systemConsumerConnector != null) {
            systemConsumerConnector.getConsumerNegotiationManager().handleAgreement(compacted);
        }
    }

    @Override
    public void finalize(Map<String, Object> event, boolean b) {
        var compacted = processJsonLd(event, createDspContext());
        if (systemConsumerConnector != null) {
            systemConsumerConnector.getConsumerNegotiationManager().handleFinalized(compacted);
        }
    }

    @Override
    public Map<String, Object> getNegotiation(String providerPid) {
        if (systemConsumerConnector != null) {
            var negotiation = systemConsumerConnector.getConsumerNegotiationManager().findById(providerPid);
            var consumerPid = negotiation.getCorrelationId();
            var state = DSPACE_NAMESPACE + negotiation.getState().toString();
            return processJsonLd(createNegotiationResponse(providerPid, consumerPid, state), createDspContext());
        } else {
            try (var response = getJson(consumerConnectorBaseUrl + format(GET_PATH, providerPid))) {
                //noinspection DataFlowIssue
                var jsonResponse = processJsonLd(response.body().byteStream(), createDspContext());
                var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, jsonResponse); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
                var state = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, jsonResponse); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
                monitor.debug(format("Received negotiation status response with state %s: %s", state, providerId));
                return jsonResponse;
            }
        }
    }
}
