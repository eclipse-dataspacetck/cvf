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

package org.eclipse.dataspacetck.dsp.system.api.connector;

import org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.requireNonNull;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSP_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringProperty;

/**
 * Manages contract negotiations on a provider.
 */
public class ProviderNegotiationManager {
    private Map<String, ContractNegotiation> negotiations = new ConcurrentHashMap<>();

    private Queue<ProviderNegotiationListener> listeners = new ConcurrentLinkedQueue<>();

    /**
     * Called when a contract request is received.
     */
    public ContractNegotiation handleContractRequest(Map<String, Object> contractRequest) {
        var correlationId = stringProperty(ID, contractRequest);  // correlation id is the @id of the message

        var processId = (String) contractRequest.get(DSP_NAMESPACE + "processId");

        if (processId != null) {
            // the message is a counter-offer
            return handleCounterOffer(contractRequest, processId);
        } else {
            // the message is an initial request
            return handleInitialRequest(contractRequest, correlationId);
        }
    }

    public void handleConsumerAgreed(String processId) {
        var negotiation = negotiations.get(processId);
        negotiation.transition(ContractNegotiation.State.CONSUMER_AGREED, n -> listeners.forEach(l -> l.consumerAgreed(negotiation)));
    }

    public void handleConsumerVerified(String processId, Map<String, Object> verification) {
        var negotiation = findById(processId);
        // TODO verify message
        negotiation.transition(ContractNegotiation.State.CONSUMER_VERIFIED, n -> listeners.forEach(l -> l.consumerVerified(verification, n)));
    }

    public void terminate(Map<String, Object> termination) {
        var processId = requireNonNull(stringProperty(DSP_NAMESPACE + "processId", termination));
        var negotiation = negotiations.get(processId);
        negotiation.transition(ContractNegotiation.State.TERMINATED, n -> listeners.forEach(l -> l.terminated(n)));
    }

    @NotNull
    private ContractNegotiation handleCounterOffer(Map<String, Object> contractRequest, String processId) {
        var negotiation = findById(processId);
        var offer = MessageFunctions.mapProperty(DSP_NAMESPACE + "offer", contractRequest);
        negotiation.storeOffer(offer, ContractNegotiation.State.CONSUMER_REQUESTED, n -> listeners.forEach(l -> l.contractRequested(contractRequest, negotiation)));
        return negotiation;
    }

    @NotNull
    private ContractNegotiation handleInitialRequest(Map<String, Object> contractRequest, String correlationId) {
        var previousNegotiation = findByCorrelationId(correlationId);
        if (previousNegotiation != null) {
            return previousNegotiation;
        }

        var offerId = stringProperty(DSP_NAMESPACE + "offerId", contractRequest);
        var datasetId = stringProperty(DSP_NAMESPACE + "datasetId", contractRequest);
        var callbackAddress = stringProperty(DSP_NAMESPACE + "callbackAddress", contractRequest);

        var builder = ContractNegotiation.Builder.newInstance()
                .correlationId(correlationId)
                .offerId(offerId)
                .state(ContractNegotiation.State.CONSUMER_REQUESTED)
                .callbackAddress(callbackAddress)
                .datasetId(datasetId);

        var negotiation = builder.build();
        negotiations.put(negotiation.getId(), negotiation);
        listeners.forEach(l -> l.contractRequested(contractRequest, negotiation));

        return negotiation;
    }

    @NotNull
    public ContractNegotiation findById(String id) {
        var negotiation = negotiations.get(id);
        if (negotiation == null) {
            throw new IllegalArgumentException("Contract negotiation not found for id: " + id);
        }
        return negotiation;
    }

    @Nullable
    public ContractNegotiation findByCorrelationId(String id) {
        return negotiations.values().stream()
                .filter(n -> id.equals(n.getCorrelationId()))
                .findAny().orElse(null);
    }

    public Map<String, ContractNegotiation> getNegotiations() {
        return negotiations;
    }

    public void registerListener(ProviderNegotiationListener listener) {
        listeners.add(listener);
    }

    public void deregisterListener(ProviderNegotiationListener listener) {
        listeners.remove(listener);
    }

}
