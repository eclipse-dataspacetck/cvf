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
import org.eclipse.dataspacetck.dsp.system.client.ProviderNegotiationClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.ProviderNegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
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
 * A pipeline that drives message interactions with a provider connector under test. Uses a TCK consumer connector to interact with
 * the provider connector being verified.
 */
public class ProviderNegotiationPipelineImpl extends AbstractNegotiationPipeline<ProviderNegotiationPipeline> implements ProviderNegotiationPipeline {
    private static final String NEGOTIATIONS_OFFER_PATH = "/negotiations/[^/]+/offer/";
    private static final String NEGOTIATIONS_AGREEMENT_PATH = "/negotiations/[^/]+/agreement";
    private static final String NEGOTIATIONS_TERMINATION_PATH = "/negotiations/[^/]+/termination";
    private static final String NEGOTIATION_EVENT_PATH = "/negotiations/[^/]+/events";

    private Connector consumerConnector;
    private ProviderNegotiationClient negotiationClient;

    public ProviderNegotiationPipelineImpl(ProviderNegotiationClient negotiationClient,
                                           CallbackEndpoint endpoint,
                                           Connector connector,
                                           Monitor monitor,
                                           long waitTime) {
        super(endpoint, monitor, waitTime);
        this.negotiationClient = negotiationClient;
        this.consumerConnector = connector;
        this.monitor = monitor;
    }

    @SuppressWarnings("unused")
    public ProviderNegotiationPipeline sendRequestMessage(String datasetId, String offerId) {
        stages.add(() -> {
            negotiation = consumerConnector.getConsumerNegotiationManager().createNegotiation(datasetId, offerId);

            var contractRequest = createContractRequest(negotiation.getId(), offerId, datasetId, endpoint.getAddress());

            monitor.debug("Sending contract request");
            var response = negotiationClient.contractRequest(contractRequest, false);
            var correlationId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, response); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
            consumerConnector.getConsumerNegotiationManager().contractRequested(negotiation.getId(), correlationId);
        });
        return this;
    }

    public ProviderNegotiationPipeline sendCounterOfferMessage(String offerId, String targetId) {
        return sendCounterOfferMessage(offerId, targetId, false);
    }

    public ProviderNegotiationPipeline sendCounterOfferMessage(String offerId, String targetId, boolean expectError) {
        stages.add(() -> {
            var providerId = negotiation.getCorrelationId();
            var consumerId = negotiation.getId();
            var contractRequest = createCounterOffer(providerId, consumerId, offerId, targetId, endpoint.getAddress());

            monitor.debug("Sending counter offer: " + providerId);
            if (!expectError) {
                consumerConnector.getConsumerNegotiationManager().counterOffered(consumerId);
            }
            negotiationClient.contractRequest(contractRequest, expectError);
        });
        return this;
    }

    public ProviderNegotiationPipeline sendTermination() {
        return sendTermination(false);
    }

    public ProviderNegotiationPipeline sendTermination(boolean expectError) {
        stages.add(() -> {
            pause();
            var providerId = negotiation.getCorrelationId();
            var consumerId = negotiation.getId();
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
            var providerId = negotiation.getCorrelationId();
            var consumerId = negotiation.getId();
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
            var providerId = negotiation.getCorrelationId();
            var consumerId = negotiation.getId();
            monitor.debug("Sending verification: " + providerId);
            if (!expectError) {
                consumerConnector.getConsumerNegotiationManager().verified(consumerId);
            }
            negotiationClient.verify(createVerification(providerId, consumerId), expectError);
        });
        return this;
    }

    public ProviderNegotiationPipeline then(Runnable runnable) {
        stages.add(runnable);
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
        return addHandlerAction(NEGOTIATIONS_TERMINATION_PATH, d -> negotiation.transition(TERMINATED));
    }

    @SuppressWarnings("unused")
    public ProviderNegotiationPipeline thenVerify(Runnable runnable) {
        return then(runnable);
    }

    @SuppressWarnings("unused")
    public ProviderNegotiationPipeline thenVerifyNegotiation(Consumer<ContractNegotiation> consumer) {
        return then(() -> consumer.accept(negotiation));
    }

    @SuppressWarnings("unused")
    public ProviderNegotiationPipeline thenVerifyState(ContractNegotiation.State state) {
        stages.add(() -> assertEquals(state, negotiation.getState()));
        return this;
    }

    public ProviderNegotiationPipeline thenVerifyProviderState(ContractNegotiation.State state) {
        stages.add(() -> {
            pause();
            var providerNegotiation = negotiationClient.getNegotiation(negotiation.getCorrelationId());
            var actual = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, providerNegotiation);
            assertEquals(DSPACE_NAMESPACE + state.toString(), actual);
        });
        return this;
    }


}
