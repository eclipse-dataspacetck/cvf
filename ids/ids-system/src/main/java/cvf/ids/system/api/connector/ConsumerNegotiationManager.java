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

package cvf.ids.system.api.connector;

import cvf.ids.system.api.statemachine.ContractNegotiation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static cvf.ids.system.api.message.IdsConstants.IDS_NAMESPACE;
import static cvf.ids.system.api.message.MessageFunctions.stringProperty;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_AGREED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_REQUESTED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_VERIFIED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_FINALIZED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_OFFERED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.TERMINATED;

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
        contractNegotiation.setCorrelationId(correlationId, CONSUMER_REQUESTED);
    }

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#CONSUMER_REQUESTED} when a counter-offer has been made.
     */
    public void counterOffer(String processId) {
        var contractNegotiation = getNegotiations().get(processId);
        contractNegotiation.transition(CONSUMER_REQUESTED);
    }

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#CONSUMER_AGREED} when an offer is accepted by the consumer.
     */
    public void agree(String processId) {
        var contractNegotiation = getNegotiations().get(processId);
        contractNegotiation.transition(CONSUMER_AGREED);
    }

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#CONSUMER_VERIFIED} when a verification is being sent.
     */
    public void verify(String processId) {
        var contractNegotiation = getNegotiations().get(processId);
        contractNegotiation.transition(CONSUMER_VERIFIED);
    }

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#TERMINATED}.
     */
    public void terminate(String processId) {
        var negotiation = getNegotiations().get(processId);
        negotiation.transition(TERMINATED, n -> listeners.forEach(l -> l.terminated(n)));
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
    public void handleProviderOffer(Map<String, Object> offer) {
        var id = stringProperty(IDS_NAMESPACE + "processId", offer);
        var negotiation = findByCorrelationId(id);
        negotiation.storeOffer(offer, PROVIDER_OFFERED);
    }

    /**
     * Processes an agreement received from the provider.
     */
    public void handleAgreement(Map<String, Object> agreement) {
        var id = stringProperty(IDS_NAMESPACE + "processId", agreement);
        var negotiation = findByCorrelationId(id);
        negotiation.storeAgreement(agreement);
    }

    /**
     * Processes a finalize event received from the provider.
     */
    public void handleFinalized(Map<String, Object> event) {
        var id = stringProperty(IDS_NAMESPACE + "processId", event);
        var negotiation = findByCorrelationId(id);
        negotiation.transition(PROVIDER_FINALIZED);
    }

    @NotNull
    public ContractNegotiation findByCorrelationId(String id) {
        return negotiations.values().stream()
                .filter(n -> id.equals(n.getCorrelationId()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Negotiation not found for correlation id: " + id));
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

}
