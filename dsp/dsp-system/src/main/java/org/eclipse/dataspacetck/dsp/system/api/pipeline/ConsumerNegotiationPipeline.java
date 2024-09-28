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

package org.eclipse.dataspacetck.dsp.system.api.pipeline;

import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.client.ConsumerNegotiationClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.NegotiationListener;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static java.util.UUID.randomUUID;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createAgreement;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createDspContext;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createFinalizedEvent;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createOffer;

/**
 * A pipeline that drives message interactions with a consumer connector under test. Uses a TCK provider connector to interact with
 * the consumer connector being verified.
 */
public class ConsumerNegotiationPipeline extends AbstractNegotiationPipeline<ConsumerNegotiationPipeline> {
    private static final String NEGOTIATION_EVENT_PATH = "/callback/negotiations/[^/]+/events";
    private static final String VERIFICATION_PATH = "/callback/negotiations/callback/%s/agreement/verification";

    private ConsumerNegotiationClient negotiationClient;
    private Connector providerConnector;
    private CallbackEndpoint endpoint;

    public ConsumerNegotiationPipeline(ConsumerNegotiationClient negotiationClient,
                                       CallbackEndpoint endpoint,
                                       Connector providerConnector,
                                       Monitor monitor,
                                       long waitTime) {
        super(endpoint, monitor, waitTime);
        this.negotiationClient = negotiationClient;
        this.providerConnector = providerConnector;
        this.endpoint = endpoint;
    }

    /**
     * Initiates a request with the consumer connector being verified. Consumer connectors are required to expose an HTTP GET
     * endpoint to initiate requests.
     */
    public ConsumerNegotiationPipeline initiateRequest(String datasetId, String offerId) {
        stages.add(() -> {
            // Register a listener to record the negotiation that is created when the TCK connector receives the
            // negotiation request from the consumer under test.
            providerConnector.getProviderNegotiationManager().registerListener(new NegotiationListener() {
                @Override
                public void contractRequested(ContractNegotiation negotiation) {
                    ConsumerNegotiationPipeline.this.negotiation = negotiation;
                    // Remove the listener
                    providerConnector.getProviderNegotiationManager().deregisterListener(this);
                }
            });
            // call the consumer endpoint
            negotiationClient.initiateRequest(datasetId, offerId);
        });
        return this;
    }

    /**
     * Sends an offer to the consumer connector being verified.
     */
    public ConsumerNegotiationPipeline sendOfferMessage() {
        stages.add(() -> {
            var providerId = negotiation.getId();
            var consumerId = negotiation.getCorrelationId();
            var offerId = negotiation.getOfferId();
            var datasetId = negotiation.getDatasetId();
            var offer = createOffer(providerId, consumerId, offerId, datasetId);
            monitor.debug("Sending offer");
            negotiationClient.contractOffer(offer, false);
            providerConnector.getProviderNegotiationManager().offered(providerId);
        });
        return this;
    }

    /**
     * Sends an agreement to the consumer connector being verified.
     */
    public ConsumerNegotiationPipeline sendAgreementMessage() {
        stages.add(() -> {
            var providerId = negotiation.getId();
            var consumerId = negotiation.getCorrelationId();
            var agreement = createAgreement(providerId, consumerId, randomUUID().toString(), negotiation.getDatasetId());
            monitor.debug("Sending agreement");
            negotiationClient.contractAgreement(agreement);
            providerConnector.getProviderNegotiationManager().agreed(providerId);
        });
        return this;
    }

    /**
     * Sends a finalized event to the consumer connector being verified.
     */
    public ConsumerNegotiationPipeline sendFinalizedEvent() {
        stages.add(() -> {
            var providerId = negotiation.getId();
            var consumerId = negotiation.getCorrelationId();
            var event = createFinalizedEvent(providerId, consumerId);
            monitor.debug("Sending finalized event");
            negotiationClient.finalize(event, false);
            providerConnector.getProviderNegotiationManager().finalized(providerId);
        });
        return this;
    }

    /**
     * Expect an accepted event to be received and execute the given action..
     */
    public ConsumerNegotiationPipeline expectAcceptedEvent(Consumer<Map<String, Object>> action) {
        return expectResponse(NEGOTIATION_EVENT_PATH, action);
    }

    /**
     * Expect a verification message to be received and execute the given action..
     */
    public ConsumerNegotiationPipeline expectVerifiedMessage(Consumer<Map<String, Object>> action) {
        return expectResponse(VERIFICATION_PATH, action);
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
