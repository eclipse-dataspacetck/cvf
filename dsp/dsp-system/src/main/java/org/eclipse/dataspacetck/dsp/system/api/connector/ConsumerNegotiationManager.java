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

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createOfferAck;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringProperty;

/**
 * Manages contract negotiations on a consumer.
 */
public class ConsumerNegotiationManager {

    private Map<String, ContractNegotiation> negotiations = new ConcurrentHashMap<>();

    private Queue<ConsumerNegotiationListener> listeners = new ConcurrentLinkedQueue<>();

    /**
     * Called after a contract has been requested and the negotiation id is returned by the provider. The provider negotiation id will be set as the correlation id
     * on the consumer.
     */
    public void contractRequested(String processId, String correlationId) {
        var contractNegotiation = getNegotiations().get(processId);
        contractNegotiation.setCorrelationId(correlationId, ContractNegotiation.State.REQUESTED);
    }

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#REQUESTED} when a counter-offer has been made.
     */
    public void counterOffer(String processId) {
        var contractNegotiation = getNegotiations().get(processId);
        contractNegotiation.transition(ContractNegotiation.State.REQUESTED);
    }

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#ACCEPTED} when an offer is accepted by the consumer.
     */
    public void agree(String processId) {
        var contractNegotiation = getNegotiations().get(processId);
        contractNegotiation.transition(ContractNegotiation.State.ACCEPTED);
    }

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#VERIFIED} when a verification is being sent.
     */
    public void verify(String processId) {
        var contractNegotiation = getNegotiations().get(processId);
        contractNegotiation.transition(ContractNegotiation.State.VERIFIED);
    }

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#TERMINATED}.
     */
    public void terminate(String processId) {
        var negotiation = getNegotiations().get(processId);
        negotiation.transition(ContractNegotiation.State.TERMINATED, n -> listeners.forEach(l -> l.terminated(n)));
    }

    /**
     * Creates a contract negotiation for the given dataset.
     */
    public ContractNegotiation createNegotiation(String datasetId) {
        var negotiation = ContractNegotiation.Builder.newInstance().datasetId(datasetId).build();
        negotiations.put(negotiation.getId(), negotiation);

        listeners.forEach(l -> l.negotiationCreated(negotiation));

        return negotiation;
    }

    /**
     * Processes an offer received from the provider.
     */
    public Map<String, Object> handleProviderOffer(Map<String, Object> offer) {
        var id = stringProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, offer);
        var negotiation = findById(id);
        negotiation.storeOffer(offer, ContractNegotiation.State.OFFERED);
        return createOfferAck(negotiation.getCorrelationId(), negotiation.getId(), ContractNegotiation.State.OFFERED);
    }

    /**
     * Processes an agreement received from the provider.
     */
    public void handleAgreement(Map<String, Object> agreement) {
        var id = stringProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, agreement);
        var negotiation = findById(id);
        negotiation.storeAgreement(agreement);
    }

    /**
     * Processes a finalize event received from the provider.
     */
    public void handleFinalized(Map<String, Object> event) {
        var id = stringProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, event);
        var negotiation = findById(id);
        negotiation.transition(ContractNegotiation.State.FINALIZED);
    }

    public Map<String, ContractNegotiation> getNegotiations() {
        return negotiations;
    }

    public void registerListener(ConsumerNegotiationListener listener) {
        listeners.add(listener);
    }

    public void deregisterListener(ConsumerNegotiationListener listener) {
        listeners.remove(listener);
    }

    @NotNull
    private ContractNegotiation findById(String id) {
        var negotiation = negotiations.get(id);
        if (negotiation == null) {
            throw new IllegalArgumentException("Contract negotiation not found for id: " + id);
        }
        return negotiation;
    }
}
