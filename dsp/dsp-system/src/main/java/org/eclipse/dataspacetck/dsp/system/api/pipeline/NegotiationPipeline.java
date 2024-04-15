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
import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.dsp.system.api.client.NegotiationClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSP_NAMESPACE_PREFIX;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createAcceptedEvent;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createContractRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createCounterOffer;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createTermination;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createVerification;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test pipeline to create and execute message interaction tests.
 */
public class NegotiationPipeline {
    private static final int DEFAULT_WAIT_SECONDS = 15;

    private static final String NEGOTIATIONS_OFFER_PATH = "/negotiations/[^/]+/offer/";
    private static final String NEGOTIATIONS_AGREEMENT_PATH = "/negotiations/[^/]+/agreement";
    private static final String NEGOTIATIONS_TERMINATION_PATH = "/negotiations/[^/]+/termination";
    private static final String NEGOTIATION_EVENT_PATH = "/negotiations/[^/]+/events";

    private CallbackEndpoint endpoint;
    private Connector connector;
    private NegotiationClient negotiationClient;

    private long waitTime = DEFAULT_WAIT_SECONDS;
    private List<Runnable> stages = new ArrayList<>();

    private ContractNegotiation clientNegotiation;

    public static NegotiationPipeline negotiationPipeline(NegotiationClient negotiationClient, CallbackEndpoint endpoint, Connector connector) {
        var pipeline = new NegotiationPipeline();
        pipeline.negotiationClient = negotiationClient;
        pipeline.connector = connector;
        pipeline.endpoint = endpoint;
        return pipeline;
    }

    @SuppressWarnings("unused")
    public NegotiationPipeline waitTime(long waitTime) {
        this.waitTime = waitTime;
        return this;
    }

    public NegotiationPipeline sendRequest(String datasetId, String offerId) {
        stages.add(() -> {
            clientNegotiation = connector.getConsumerNegotiationManager().createNegotiation(datasetId);

            var contractRequest = createContractRequest(clientNegotiation.getId(), offerId, datasetId, endpoint.getAddress());

            var response = negotiationClient.contractRequest(contractRequest);
            var correlationId = stringProperty(ID, response);
            connector.getConsumerNegotiationManager().contractRequested(clientNegotiation.getId(), correlationId);
        });
        return this;
    }

    public NegotiationPipeline sendCounterRequest() {
        stages.add(() -> {
            var contractRequest = createCounterOffer(clientNegotiation.getCorrelationId(), clientNegotiation.getDatasetId());
            connector.getConsumerNegotiationManager().counterOffer(clientNegotiation.getId());
            negotiationClient.contractRequest(contractRequest);
        });
        return this;
    }

    public NegotiationPipeline sendTermination() {
        stages.add(() -> {
            var termination = createTermination(clientNegotiation.getCorrelationId(), "1");
            negotiationClient.terminate(termination);
            connector.getConsumerNegotiationManager().terminate(clientNegotiation.getId());
        });
        return this;
    }

    public NegotiationPipeline acceptLastOffer() {
        stages.add(() -> {
            connector.getConsumerNegotiationManager().agree(clientNegotiation.getId());
            negotiationClient.consumerAgree(createAcceptedEvent(clientNegotiation.getCorrelationId()));
        });
        return this;
    }

    public NegotiationPipeline sendConsumerVerify() {
        stages.add(() -> {
            connector.getConsumerNegotiationManager().verify(clientNegotiation.getId());
            negotiationClient.consumerVerify(createVerification(clientNegotiation.getCorrelationId()));
        });
        return this;
    }

    public NegotiationPipeline then(Runnable runnable) {
        stages.add(runnable);
        return this;
    }

    public NegotiationPipeline thenWaitForState(ContractNegotiation.State state) {
        return thenWait("state to transition to " + state, () -> state == clientNegotiation.getState());
    }

    public NegotiationPipeline thenWait(String description, Callable<Boolean> condition) {
        stages.add(() -> {
            try {
                await().atMost(waitTime, SECONDS).until(condition);
            } catch (ConditionTimeoutException e) {
                throw new AssertionError("Timeout waiting for " + description);
            }
        });
        return this;
    }

    public NegotiationPipeline expectOffer(Consumer<Map<String, Object>> action) {
        stages.add(() ->
                endpoint.registerHandler(NEGOTIATIONS_OFFER_PATH, offer -> {
                    //noinspection unchecked
                    action.accept((Map<String, Object>) offer);
                    endpoint.deregisterHandler(NEGOTIATIONS_OFFER_PATH);
                    return null;
                }));
        return this;
    }

    public NegotiationPipeline expectAgreement(Consumer<Map<String, Object>> action) {
        stages.add(() ->
                endpoint.registerHandler(NEGOTIATIONS_AGREEMENT_PATH, agreement -> {
                    //noinspection unchecked
                    action.accept((Map<String, Object>) agreement);
                    endpoint.deregisterHandler(NEGOTIATIONS_AGREEMENT_PATH);
                    return null;
                }));
        return this;
    }

    public NegotiationPipeline expectFinalized(Consumer<Map<String, Object>> action) {
        stages.add(() ->
                endpoint.registerHandler(NEGOTIATION_EVENT_PATH, agreement -> {
                    //noinspection unchecked
                    action.accept((Map<String, Object>) agreement);
                    endpoint.deregisterHandler(NEGOTIATION_EVENT_PATH);
                    return null;
                }));
        return this;
    }

    public NegotiationPipeline expectTermination() {
        stages.add(() ->
                endpoint.registerHandler(NEGOTIATIONS_TERMINATION_PATH, termination -> {
                    clientNegotiation.transition(ContractNegotiation.State.TERMINATED);
                    endpoint.deregisterHandler(NEGOTIATIONS_TERMINATION_PATH);
                    return null;
                }));
        return this;
    }

    @SuppressWarnings("unused")
    public NegotiationPipeline thenVerify(Runnable runnable) {
        return then(runnable);
    }

    @SuppressWarnings("unused")
    public NegotiationPipeline thenVerifyNegotiation(Consumer<ContractNegotiation> consumer) {
        return then(() -> consumer.accept(clientNegotiation));
    }

    @SuppressWarnings("unused")
    public NegotiationPipeline thenVerifyState(ContractNegotiation.State state) {
        stages.add(() -> assertEquals(state, clientNegotiation.getState()));
        return this;
    }

    public NegotiationPipeline thenVerifyProviderState(ContractNegotiation.State state) {
        stages.add(() -> {
            var providerNegotiation = negotiationClient.getNegotiation(clientNegotiation.getCorrelationId());
            assertEquals(state, ContractNegotiation.State.valueOf(stringProperty(DSP_NAMESPACE_PREFIX + "state", providerNegotiation).toUpperCase())); // TODO JSON-LD
        });
        return this;
    }

    public void execute() {
        stages.forEach(Runnable::run);
    }

}
