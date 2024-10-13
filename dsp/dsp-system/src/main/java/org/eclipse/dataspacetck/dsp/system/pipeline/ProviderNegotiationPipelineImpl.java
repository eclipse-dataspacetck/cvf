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

package org.eclipse.dataspacetck.dsp.system.pipeline;

import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.ProviderNegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State;
import org.eclipse.dataspacetck.dsp.system.client.ProviderNegotiationClient;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TCK_PARTICIPANT_ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createAcceptedEvent;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createContractRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createCounterOffer;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createDspContext;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createTermination;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createVerification;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.TERMINATED;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Default implementation.
 */
public class ProviderNegotiationPipelineImpl extends AbstractNegotiationPipeline<ProviderNegotiationPipeline> implements ProviderNegotiationPipeline {
    private static final String NEGOTIATIONS_OFFER_PATH = "/negotiations/[^/]+/offers/";
    private static final String NEGOTIATIONS_AGREEMENT_PATH = "/negotiations/[^/]+/agreement";
    private static final String NEGOTIATIONS_TERMINATION_PATH = "/negotiations/[^/]+/termination/";
    private static final String NEGOTIATION_EVENT_PATH = "/negotiations/[^/]+/events";

    private Connector consumerConnector;
    private String providerConnectorId;
    private ProviderNegotiationClient negotiationClient;

    public ProviderNegotiationPipelineImpl(ProviderNegotiationClient negotiationClient,
                                           CallbackEndpoint endpoint,
                                           Connector connector,
                                           String providerConnectorId,
                                           Monitor monitor,
                                           long waitTime) {
        super(endpoint, monitor, waitTime);
        this.negotiationClient = negotiationClient;
        this.consumerConnector = connector;
        this.providerConnectorId = providerConnectorId;
        this.monitor = monitor;
    }

    @SuppressWarnings("unused")
    public ProviderNegotiationPipeline sendRequestMessage(String datasetId, String offerId) {
        stages.add(() -> {
            providerNegotiation = consumerConnector.getConsumerNegotiationManager().createNegotiation(datasetId, offerId);

            var contractRequest = createContractRequest(providerNegotiation.getId(), offerId, datasetId, endpoint.getAddress());

            monitor.debug("Sending contract request");
            var response = negotiationClient.contractRequest(contractRequest, providerConnectorId, false);
            var correlationId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, response); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
            consumerConnector.getConsumerNegotiationManager().contractRequested(providerNegotiation.getId(), correlationId);
        });
        return this;
    }

    public ProviderNegotiationPipeline sendCounterOfferMessage(String offerId, String targetId) {
        return sendCounterOfferMessage(offerId, targetId, false);
    }

    public ProviderNegotiationPipeline sendCounterOfferMessage(String offerId, String targetId, boolean expectError) {
        stages.add(() -> {
            var providerId = providerNegotiation.getCorrelationId();
            var consumerId = providerNegotiation.getId();
            var assigner = providerNegotiation.getCounterPartyId();
            var contractRequest = createCounterOffer(providerId,
                    consumerId,
                    offerId,
                    assigner,
                    TCK_PARTICIPANT_ID,
                    targetId,
                    endpoint.getAddress());

            monitor.debug("Sending counter offer: " + providerId);
            if (!expectError) {
                consumerConnector.getConsumerNegotiationManager().counterOffered(consumerId);
            }
            negotiationClient.contractRequest(contractRequest, providerConnectorId, expectError);
        });
        return this;
    }

    public ProviderNegotiationPipeline sendTermination() {
        return sendTermination(false);
    }

    public ProviderNegotiationPipeline sendTermination(boolean expectError) {
        stages.add(() -> {
            pause();
            var providerId = providerNegotiation.getCorrelationId();
            var consumerId = providerNegotiation.getId();
            var termination = createTermination(providerId, consumerId, "1");

            monitor.debug("Sending termination: " + providerId);
            negotiationClient.terminate(termination, expectError);
            if (!expectError) {
                consumerConnector.getConsumerNegotiationManager().terminated(consumerId);
            }
        });
        return this;
    }

    public ProviderNegotiationPipeline acceptLastOffer() {
        stages.add(() -> {
            var providerId = providerNegotiation.getCorrelationId();
            var consumerId = providerNegotiation.getId();
            monitor.debug("Accepting offer: " + providerId);
            consumerConnector.getConsumerNegotiationManager().accepted(consumerId);
            negotiationClient.accept(createAcceptedEvent(providerId, consumerId));
        });
        return this;
    }

    public ProviderNegotiationPipeline sendVerifiedEvent() {
        return sendVerifiedEvent(false);
    }

    public ProviderNegotiationPipeline sendVerifiedEvent(boolean expectError) {
        stages.add(() -> {
            pause();
            var providerId = providerNegotiation.getCorrelationId();
            var consumerId = providerNegotiation.getId();
            monitor.debug("Sending verification: " + providerId);
            if (!expectError) {
                consumerConnector.getConsumerNegotiationManager().verified(consumerId);
            }
            negotiationClient.verify(createVerification(providerId, consumerId), expectError);
        });
        return this;
    }

    public ProviderNegotiationPipeline expectOfferMessage(Function<Map<String, Object>, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(NEGOTIATIONS_OFFER_PATH, offer -> {
                    var negotiation = action.apply((processJsonLd(offer, createDspContext())));
                    endpoint.deregisterHandler(NEGOTIATIONS_OFFER_PATH);
                    latch.countDown();
                    return serialize(negotiation);
                }));
        return this;
    }

    public ProviderNegotiationPipeline expectAgreementMessage(Consumer<Map<String, Object>> action) {
        return addHandlerAction(NEGOTIATIONS_AGREEMENT_PATH, action);
    }

    public ProviderNegotiationPipeline expectFinalizedEvent(Consumer<Map<String, Object>> action) {
        return addHandlerAction(NEGOTIATION_EVENT_PATH, action);
    }

    public ProviderNegotiationPipeline expectTermination() {
        return addHandlerAction(NEGOTIATIONS_TERMINATION_PATH, d -> providerNegotiation.transition(TERMINATED));
    }

    @SuppressWarnings("unused")
    public ProviderNegotiationPipeline thenVerifyNegotiation(Consumer<ContractNegotiation> consumer) {
        return then(() -> consumer.accept(providerNegotiation));
    }

    @SuppressWarnings("unused")
    public ProviderNegotiationPipeline thenVerifyState(State state) {
        stages.add(() -> assertEquals(state, providerNegotiation.getState()));
        return this;
    }

    public ProviderNegotiationPipeline thenVerifyProviderState(State state) {
        stages.add(() -> {
            pause();
            var providerNegotiation = negotiationClient.getNegotiation(this.providerNegotiation.getCorrelationId());
            var actual = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, providerNegotiation);
            assertEquals(DSPACE_NAMESPACE + state.toString(), actual);
        });
        return this;
    }


}
