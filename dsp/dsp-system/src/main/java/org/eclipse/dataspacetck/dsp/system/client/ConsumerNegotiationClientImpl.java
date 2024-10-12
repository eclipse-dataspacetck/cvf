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
import org.eclipse.dataspacetck.dsp.system.api.metadata.DspTestingWorkaround;

import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.getJson;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TCK_PARTICIPANT_ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createDspContext;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createNegotiationResponse;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringIdProperty;

/**
 * Default implementation that supports dispatch to a local, in-memory test connector or a remote connector system via HTTP.
 */
public class ConsumerNegotiationClientImpl implements ConsumerNegotiationClient {
    private static final String GET_PATH = "%s/negotiations/%s";

    @DspTestingWorkaround("Should be %s/negotiations/%s/offers")
    private static final String OFFERS_PATH = "%s/negotiations/%s/offer";
    private static final String AGREEMENTS_PATH = "%s/negotiations/%s/agreement";
    private static final String FINALIZE_PATH = "%s/negotiations/%s/events";

    private String consumerConnectorInitiateUrl;
    private Connector systemConsumerConnector;
    private Connector providerConnector;
    private String providerConnectorBaseUrl;
    private Monitor monitor;

    public ConsumerNegotiationClientImpl(String consumerConnectorInitiateUrl,
                                         Connector providerConnector,
                                         String providerConnectorBaseUrl,
                                         Monitor monitor) {
        this.consumerConnectorInitiateUrl = consumerConnectorInitiateUrl;
        this.providerConnector = providerConnector;
        this.providerConnectorBaseUrl = providerConnectorBaseUrl;
        this.monitor = monitor;
    }

    public ConsumerNegotiationClientImpl(Connector consumerConnector,
                                         Connector providerConnector,
                                         Monitor monitor) {
        this.systemConsumerConnector = consumerConnector;
        this.providerConnector = providerConnector;
        this.monitor = monitor;
    }

    @Override
    public void initiateRequest(String datasetId, String offerId) {
        if (systemConsumerConnector != null) {
            systemConsumerConnector.getConsumerNegotiationManager().createNegotiation(datasetId, offerId);
        } else {
            var request = Map.of("providerId", TCK_PARTICIPANT_ID, "offerId", offerId, "connectorAddress", providerConnectorBaseUrl);
            try (var response = postJson(consumerConnectorInitiateUrl, request, false, true)) {
                monitor.debug("Received contract request response");
            }
        }
    }

    @Override
    public void contractOffer(String consumerId, Map<String, Object> offer, String callbackAddress, boolean expectError) {
        var compacted = processJsonLd(offer, createDspContext());
        if (systemConsumerConnector != null) {
            systemConsumerConnector.getConsumerNegotiationManager().handleOffer(compacted);
        } else {
            try (var response = postJson(format(OFFERS_PATH, callbackAddress, consumerId), offer, expectError)) {
                monitor.debug("Received contract request response");
                // TODO Validate response
                // processJsonLd(response.body().byteStream(), createDspContext());
            }
        }
    }

    @Override
    public void contractAgreement(String consumerId, Map<String, Object> agreement, String callbackAddress) {
        var compacted = processJsonLd(agreement, createDspContext());
        if (systemConsumerConnector != null) {
            systemConsumerConnector.getConsumerNegotiationManager().handleAgreement(compacted);
        } else {
            try (var response = postJson(format(AGREEMENTS_PATH, callbackAddress, consumerId), agreement, false)) {
                monitor.debug("Received contract agreement response");
                // TODO Validate response
                // processJsonLd(response.body().byteStream(), createDspContext());
            }
        }
    }

    @Override
    public void finalize(String consumerId, Map<String, Object> event, String callbackAddress, boolean expectError) {
        var compacted = processJsonLd(event, createDspContext());
        if (systemConsumerConnector != null) {
            systemConsumerConnector.getConsumerNegotiationManager().handleFinalized(compacted);
        } else {
            try (var response = postJson(format(FINALIZE_PATH, callbackAddress, consumerId), event, false)) {
                monitor.debug("Received contract finalize response");
                // TODO Validate response
                // processJsonLd(response.body().byteStream(), createDspContext());
            }
        }
    }

    @Override
    public Map<String, Object> getNegotiation(String consumerId, String callbackAddress) {
        if (systemConsumerConnector != null) {
            var negotiation = systemConsumerConnector.getConsumerNegotiationManager().findById(consumerId);
            var consumerPid = negotiation.getCorrelationId();
            var state = DSPACE_NAMESPACE + negotiation.getState().toString();
            return processJsonLd(createNegotiationResponse(consumerId, consumerPid, state), createDspContext());
        } else {
            try (var response = getJson(format(GET_PATH, callbackAddress, consumerId))) {
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
