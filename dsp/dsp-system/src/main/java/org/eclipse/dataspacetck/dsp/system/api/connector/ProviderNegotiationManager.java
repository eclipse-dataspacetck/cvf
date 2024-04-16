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

import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.requireNonNull;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CALLBACK_ADDRESS_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_OFFER_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.mapProperty;
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
        var processId = (String) contractRequest.get(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED);

        if (processId != null) {
            // the message is a counter-offer
            return handleCounterOffer(contractRequest, processId);
        } else {
            // the message is an initial request
            return handleInitialRequest(contractRequest);
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
        var processId = requireNonNull(stringProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, termination));
        var negotiation = negotiations.get(processId);
        negotiation.transition(ContractNegotiation.State.TERMINATED, n -> listeners.forEach(l -> l.terminated(n)));
    }

    @NotNull
    private ContractNegotiation handleCounterOffer(Map<String, Object> contractRequest, String processId) {
        var negotiation = findById(processId);
        var offer = mapProperty(DSPACE_PROPERTY_OFFER_EXPANDED, contractRequest);
        negotiation.storeOffer(offer, ContractNegotiation.State.CONSUMER_REQUESTED, n -> listeners.forEach(l -> l.contractRequested(contractRequest, negotiation)));
        return negotiation;
    }

    @NotNull
    private ContractNegotiation handleInitialRequest(Map<String, Object> contractRequest) {
        var consumerId = stringProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, contractRequest);
        var previousNegotiation = findByCorrelationId(consumerId);
        if (previousNegotiation != null) {
            return previousNegotiation;
        }

        var offer = mapProperty(DSPACE_PROPERTY_OFFER_EXPANDED, contractRequest);

        var offerId = stringProperty(ID, offer);
        var callbackAddress = stringProperty(DSPACE_PROPERTY_CALLBACK_ADDRESS_EXPANDED, contractRequest);

        var builder = ContractNegotiation.Builder.newInstance()
                .correlationId(consumerId)
                .offerId(offerId)
                .state(ContractNegotiation.State.CONSUMER_REQUESTED)
                .callbackAddress(callbackAddress);

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
