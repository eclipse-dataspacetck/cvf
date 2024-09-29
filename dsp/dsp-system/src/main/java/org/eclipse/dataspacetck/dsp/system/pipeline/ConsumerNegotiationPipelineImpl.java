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
import org.eclipse.dataspacetck.dsp.system.client.ConsumerNegotiationClient;
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
 * Default Implementation.
 */
public class ConsumerNegotiationPipelineImpl extends AbstractNegotiationPipeline<ConsumerNegotiationPipeline> implements ConsumerNegotiationPipeline {
    private static final String NEGOTIATION_EVENT_PATH = "/callback/negotiations/[^/]+/events";
    private static final String VERIFICATION_PATH = "/callback/negotiations/callback/%s/agreement/verification";

    private ConsumerNegotiationClient negotiationClient;
    private Connector providerConnector;
    private CallbackEndpoint endpoint;

    public ConsumerNegotiationPipelineImpl(ConsumerNegotiationClient negotiationClient,
                                           CallbackEndpoint endpoint,
                                           Connector providerConnector,
                                           Monitor monitor,
                                           long waitTime) {
        super(endpoint, monitor, waitTime);
        this.negotiationClient = negotiationClient;
        this.providerConnector = providerConnector;
        this.endpoint = endpoint;
    }

    public ConsumerNegotiationPipeline initiateRequest(String datasetId, String offerId) {
        stages.add(() -> {
            // Register a listener to record the negotiation that is created when the TCK connector receives the
            // negotiation request from the consumer under test.
            providerConnector.getProviderNegotiationManager().registerListener(new NegotiationListener() {
                @Override
                public void contractRequested(ContractNegotiation negotiation) {
                    ConsumerNegotiationPipelineImpl.this.negotiation = negotiation;
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

    public ConsumerNegotiationPipeline expectAcceptedEvent(Consumer<Map<String, Object>> action) {
        return expectResponse(NEGOTIATION_EVENT_PATH, action);
    }

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
