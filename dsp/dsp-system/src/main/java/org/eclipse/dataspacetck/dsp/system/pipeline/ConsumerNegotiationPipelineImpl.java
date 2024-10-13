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

package org.eclipse.dataspacetck.dsp.system.pipeline;

import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.NegotiationListener;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.ConsumerNegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State;
import org.eclipse.dataspacetck.dsp.system.client.ConsumerNegotiationClient;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static java.util.UUID.randomUUID;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TCK_PARTICIPANT_ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createAgreement;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createDspContext;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createFinalizedEvent;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createOffer;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringIdProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Default Implementation.
 */
public class ConsumerNegotiationPipelineImpl extends AbstractNegotiationPipeline<ConsumerNegotiationPipeline> implements ConsumerNegotiationPipeline {
    private static final String REQUEST_PATH = "/negotiations/request";
    private static final String NEGOTIATION_EVENT_PATH = "/negotiations/[^/]+/events";
    private static final String VERIFICATION_PATH = "/negotiations/[^/]+/agreement/verification";

    private ConsumerNegotiationClient negotiationClient;
    private Connector providerConnector;
    private CallbackEndpoint endpoint;
    private String consumerConnectorId;

    public ConsumerNegotiationPipelineImpl(ConsumerNegotiationClient negotiationClient,
                                           CallbackEndpoint endpoint,
                                           Connector providerConnector,
                                           String consumerConnectorId,
                                           Monitor monitor,
                                           long waitTime) {
        super(endpoint, monitor, waitTime);
        this.negotiationClient = negotiationClient;
        this.providerConnector = providerConnector;
        this.endpoint = endpoint;
        this.consumerConnectorId = consumerConnectorId;
    }

    public ConsumerNegotiationPipeline initiateRequest(String datasetId, String offerId) {
        stages.add(() -> {
            // Register a listener to record the negotiation that is created when the TCK connector receives the
            // negotiation request from the consumer under test.
            providerConnector.getProviderNegotiationManager().registerListener(new NegotiationListener() {
                @Override
                public void contractRequested(ContractNegotiation negotiation) {
                    ConsumerNegotiationPipelineImpl.this.providerNegotiation = negotiation;
                    // Remove the listener
                    providerConnector.getProviderNegotiationManager().deregisterListener(this);
                }
            });
            // call the consumer endpoint
            negotiationClient.initiateRequest(datasetId, offerId);
        });
        return this;
    }

    public ConsumerNegotiationPipeline sendOfferMessage() {
        stages.add(() -> {
            var providerId = providerNegotiation.getId();
            var consumerId = providerNegotiation.getCorrelationId();
            var offerId = providerNegotiation.getOfferId();
            var datasetId = providerNegotiation.getDatasetId();
            var assignee = providerNegotiation.getCounterPartyId();
            var offerMessage = createOffer(providerId, consumerId, offerId, TCK_PARTICIPANT_ID, assignee, datasetId, endpoint.getAddress());
            monitor.debug("Sending offer");
            var consumerAddress = providerNegotiation.getCallbackAddress();
            negotiationClient.contractOffer(consumerId, offerMessage, consumerAddress, false);
            providerConnector.getProviderNegotiationManager().offered(providerId);
        });
        return this;
    }

    public ConsumerNegotiationPipeline sendAgreementMessage() {
        stages.add(() -> {
            var providerId = providerNegotiation.getId();
            var consumerId = providerNegotiation.getCorrelationId();

            var agreementId = randomUUID().toString();
            var datasetId = providerNegotiation.getDatasetId();
            var agreement = createAgreement(providerId,
                    consumerId,
                    agreementId,
                    TCK_PARTICIPANT_ID,
                    consumerConnectorId,
                    datasetId);
            var callbackAddress = providerNegotiation.getCallbackAddress();
            monitor.debug("Sending agreement");
            negotiationClient.contractAgreement(consumerId, agreement, callbackAddress);
            providerConnector.getProviderNegotiationManager().agreed(providerId);
        });
        return this;
    }

    public ConsumerNegotiationPipeline sendFinalizedEvent() {
        stages.add(() -> {
            var providerId = providerNegotiation.getId();
            var consumerId = providerNegotiation.getCorrelationId();
            var event = createFinalizedEvent(providerId, consumerId);
            var callbackAddress = providerNegotiation.getCallbackAddress();
            monitor.debug("Sending finalized event");
            negotiationClient.finalize(consumerId, event, callbackAddress, false);
            providerConnector.getProviderNegotiationManager().finalized(providerId);
        });
        return this;
    }

    @Override
    public ConsumerNegotiationPipeline expectRequest(BiFunction<Map<String, Object>, String, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(REQUEST_PATH, event -> {
                    var expanded = processJsonLd(event, createDspContext());
                    var negotiation = action.apply(expanded, consumerConnectorId);
                    endpoint.deregisterHandler(REQUEST_PATH);
                    latch.countDown();
                    return serialize(processJsonLd(negotiation, createDspContext()));
                }));
        return this;
    }

    public ConsumerNegotiationPipeline expectAcceptedEvent(Consumer<Map<String, Object>> action) {
        return expectResponse(NEGOTIATION_EVENT_PATH, action);
    }

    public ConsumerNegotiationPipeline expectVerifiedMessage(Consumer<Map<String, Object>> action) {
        return expectResponse(VERIFICATION_PATH, action);
    }

    public ConsumerNegotiationPipeline thenVerifyConsumerState(State state) {
        stages.add(() -> {
            pause();
            var callbackAddress = providerNegotiation.getCallbackAddress();
            var processId = this.providerNegotiation.getCorrelationId();
            var negotiation = negotiationClient.getNegotiation(processId, callbackAddress);
            var actual = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, negotiation);
            assertEquals(DSPACE_NAMESPACE + state.toString(), actual);
        });
        return this;
    }

    @NotNull
    private ConsumerNegotiationPipeline expectResponse(String path, Consumer<Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(path, event -> {
                    var expanded = processJsonLd(event, createDspContext());
                    action.accept(expanded);
                    endpoint.deregisterHandler(path);
                    latch.countDown();
                    return null;
                }));
        return this;
    }

}
