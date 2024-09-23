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

package org.eclipse.dataspacetck.dsp.system.api.pipeline;

import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.dataspacetck.core.api.message.MessageSerializer;
import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.client.NegotiationClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
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
 * A test pipeline to create and execute message interaction tests.
 */
public class NegotiationPipeline {
    private static final CountDownLatch NO_WAIT_LATCH = new CountDownLatch(0);

    private static final String NEGOTIATIONS_OFFER_PATH = "/negotiations/[^/]+/offer/";
    private static final String NEGOTIATIONS_AGREEMENT_PATH = "/negotiations/[^/]+/agreement";
    private static final String NEGOTIATIONS_TERMINATION_PATH = "/negotiations/[^/]+/termination";
    private static final String NEGOTIATION_EVENT_PATH = "/negotiations/[^/]+/events";
    private final long waitTime;

    private Monitor monitor;

    private CallbackEndpoint endpoint;
    private Connector connector;
    private NegotiationClient negotiationClient;

    private List<Runnable> stages = new ArrayList<>();

    private ContractNegotiation negotiation;

    /*
     Used by {@link #thenWait} methods to synchronize with the completion of a recorded expectXXX(..) method to avoid message interleaving.
     For example given:

     <pre>
         .expectOffer(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
         .sendRequest(datasetId, offerId)
         .thenWaitForState(OFFERED)
         .expectAgreement(agreement -> consumerConnector.getConsumerNegotiationManager().handleAgreement(agreement))
     </pre>

     When sendRequest is executed and the pipeline waits for the OFFERED state, it must ensure that after OFFERED is set the
     expectOffer() runnable has completed before executing the next step, expectAgreement(). Otherwise, the send agreement message
     could arrive at the provider before the expectOffer() runnable is completed, which closes the HTTP socket the provider opened to
     send the offer.

     Every expectXXXX(..) method places a latch on the deque which is then popped by the subsequent {@link #thenWait} method.
     The {@link #thenWait} method waits on the latch, which is released after the expectXXXX(..) method completes.
     */
    private Deque<CountDownLatch> expectLatches = new ArrayDeque<>();

    public NegotiationPipeline(NegotiationClient negotiationClient,
                               CallbackEndpoint endpoint,
                               Connector connector,
                               Monitor monitor,
                               long waitTime) {
        this.negotiationClient = negotiationClient;
        this.connector = connector;
        this.endpoint = endpoint;
        this.monitor = monitor;
        this.waitTime = waitTime;
    }

    @SuppressWarnings("unused")
    public NegotiationPipeline sendRequest(String datasetId, String offerId) {
        stages.add(() -> {
            negotiation = connector.getConsumerNegotiationManager().createNegotiation(datasetId);

            var contractRequest = createContractRequest(negotiation.getId(), offerId, datasetId, endpoint.getAddress());

            monitor.debug("Sending contract request");
            var response = negotiationClient.contractRequest(contractRequest);
            var correlationId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, response); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
            connector.getConsumerNegotiationManager().contractRequested(negotiation.getId(), correlationId);
        });
        return this;
    }

    public NegotiationPipeline sendCounterRequest(String offerId, String targetId) {
        stages.add(() -> {
            var providerId = negotiation.getCorrelationId();
            var consumerId = negotiation.getId();
            var contractRequest = createCounterOffer(providerId, consumerId, offerId, targetId, endpoint.getAddress());

            monitor.debug("Sending counter offer: " + providerId);
            connector.getConsumerNegotiationManager().counterOffer(consumerId);
            negotiationClient.contractRequest(contractRequest);
        });
        return this;
    }

    public NegotiationPipeline sendTermination() {
        stages.add(() -> {
            pause();
            var providerId = negotiation.getCorrelationId();
            var consumerId = negotiation.getId();
            var termination = createTermination(providerId, consumerId, "1");

            monitor.debug("Sending termination: " + providerId);
            negotiationClient.terminate(termination);
            connector.getConsumerNegotiationManager().terminate(consumerId);
        });
        return this;
    }

    public NegotiationPipeline acceptLastOffer() {
        stages.add(() -> {
            var providerId = negotiation.getCorrelationId();
            var consumerId = negotiation.getId();
            monitor.debug("Accepting offer: " + providerId);
            connector.getConsumerNegotiationManager().agree(consumerId);
            negotiationClient.consumerAccept(createAcceptedEvent(providerId, consumerId));
        });
        return this;
    }

    public NegotiationPipeline sendConsumerVerify() {
        stages.add(() -> {
            pause();
            var providerId = negotiation.getCorrelationId();
            var consumerId = negotiation.getId();
            monitor.debug("Sending verification: " + providerId);
            connector.getConsumerNegotiationManager().verify(consumerId);
            negotiationClient.consumerVerify(createVerification(providerId, consumerId));
        });
        return this;
    }

    public NegotiationPipeline then(Runnable runnable) {
        stages.add(runnable);
        return this;
    }

    public NegotiationPipeline thenWaitForState(ContractNegotiation.State state) {
        return thenWait("state to transition to " + state, () -> state == negotiation.getState());
    }

    public NegotiationPipeline thenWait(String description, Callable<Boolean> condition) {
        var latch = expectLatches.isEmpty() ? NO_WAIT_LATCH : expectLatches.pop();
        stages.add(() -> {
            try {
                try {
                    if (!latch.await(waitTime, SECONDS)) {
                        throw new RuntimeException("Timeout waiting for " + description);
                    }
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    throw new RuntimeException("Interrupted while waiting for " + description, e);
                }
                await().atMost(waitTime, SECONDS).until(condition);
                monitor.debug("Done waiting for " + description);
            } catch (ConditionTimeoutException e) {
                throw new AssertionError("Timeout waiting for " + description);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return this;
    }

    public NegotiationPipeline expectOffer(Function<Map<String, Object>, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(NEGOTIATIONS_OFFER_PATH, offer -> {
                    var negotiation = action.apply((MessageSerializer.processJsonLd(offer, createDspContext())));
                    endpoint.deregisterHandler(NEGOTIATIONS_OFFER_PATH);
                    latch.countDown();
                    return MessageSerializer.serialize(negotiation);
                }));
        return this;
    }

    public NegotiationPipeline expectAgreement(Consumer<Map<String, Object>> action) {
        return addHandlerAction(NEGOTIATIONS_AGREEMENT_PATH, action);
    }

    public NegotiationPipeline expectFinalized(Consumer<Map<String, Object>> action) {
        return addHandlerAction(NEGOTIATION_EVENT_PATH, action);
    }

    public NegotiationPipeline expectTermination() {
        return addHandlerAction(NEGOTIATIONS_TERMINATION_PATH, d -> negotiation.transition(TERMINATED));
    }

    @SuppressWarnings("unused")
    public NegotiationPipeline thenVerify(Runnable runnable) {
        return then(runnable);
    }

    @SuppressWarnings("unused")
    public NegotiationPipeline thenVerifyNegotiation(Consumer<ContractNegotiation> consumer) {
        return then(() -> consumer.accept(negotiation));
    }

    @SuppressWarnings("unused")
    public NegotiationPipeline thenVerifyState(ContractNegotiation.State state) {
        stages.add(() -> assertEquals(state, negotiation.getState()));
        return this;
    }

    public NegotiationPipeline thenVerifyProviderState(ContractNegotiation.State state) {
        stages.add(() -> {
            pause();
            var providerNegotiation = negotiationClient.getNegotiation(negotiation.getCorrelationId());
            var actual = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, providerNegotiation);
            assertEquals(DSPACE_NAMESPACE + state.toString(), actual);
        });
        return this;
    }

    public void execute() {
        stages.forEach(Runnable::run);
    }

    private NegotiationPipeline addHandlerAction(String path, Consumer<Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(path, agreement -> {
                    action.accept((MessageSerializer.processJsonLd(agreement, createDspContext())));
                    endpoint.deregisterHandler(path);
                    latch.countDown();
                    return null;
                }));
        return this;
    }

    private void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
