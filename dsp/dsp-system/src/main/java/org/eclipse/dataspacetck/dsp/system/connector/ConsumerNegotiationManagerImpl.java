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

package org.eclipse.dataspacetck.dsp.system.connector;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.ConsumerNegotiationManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.NegotiationListener;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.eclipse.dataspacetck.dsp.system.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.message.MessageFunctions.createOfferAck;
import static org.eclipse.dataspacetck.dsp.system.message.MessageFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.ACCEPTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.TERMINATED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.VERIFIED;

/**
 * Manages contract negotiations on a consumer.
 */
public class ConsumerNegotiationManagerImpl implements ConsumerNegotiationManager {
    private Monitor monitor;

    private Map<String, ContractNegotiation> negotiations = new ConcurrentHashMap<>();

    private Queue<NegotiationListener> listeners = new ConcurrentLinkedQueue<>();

    public ConsumerNegotiationManagerImpl(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public ContractNegotiation createNegotiation(String datasetId, String offerId) {
        var negotiation = ContractNegotiation.Builder.newInstance()
                .datasetId(datasetId)
                .offerId(offerId)
                .build();
        negotiations.put(negotiation.getId(), negotiation);
        return negotiation;
    }

    @Override
    public void contractRequested(String consumerId, String providerId) {
        var negotiation = getNegotiations().get(consumerId);
        negotiation.setCorrelationId(providerId, REQUESTED);
        listeners.forEach(l -> l.contractRequested(negotiation));
    }

    @Override
    public void counterOffered(String consumerId) {
        var negotiation = getNegotiations().get(consumerId);
        negotiation.transition(REQUESTED, n -> listeners.forEach(l -> l.offered(n)));
    }

    @Override
    public void accepted(String consumerId) {
        var negotiation = getNegotiations().get(consumerId);
        negotiation.transition(ACCEPTED, n -> listeners.forEach(l -> l.agreed(n)));
    }

    @Override
    public void verified(String consumerId) {
        var negotiation = getNegotiations().get(consumerId);
        negotiation.transition(VERIFIED, n -> listeners.forEach(l -> l.verified(n)));
    }

    @Override
    public void terminated(String consumerId) {
        var negotiation = getNegotiations().get(consumerId);
        negotiation.transition(TERMINATED, n -> listeners.forEach(l -> l.terminated(n)));
    }

    @Override
    public Map<String, Object> handleOffer(Map<String, Object> offer) {
        var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, offer); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
        monitor.debug("Received provider offer: " + providerId);
        var consumerId = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, offer); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
        var negotiation = findById(consumerId);
        negotiation.storeOffer(offer, OFFERED, n -> listeners.forEach(l -> l.offered(negotiation)));
        return createOfferAck(negotiation.getCorrelationId(), negotiation.getId(), OFFERED);
    }

    @Override
    public void handleAgreement(Map<String, Object> agreement) {
        var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, agreement); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
        monitor.debug("Received provider agreement: " + providerId);
        var consumerId = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, agreement); // // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
        var negotiation = findById(consumerId);
        negotiation.storeAgreement(agreement, n -> listeners.forEach(l -> l.agreed(negotiation)));
    }

    @Override
    public void handleFinalized(Map<String, Object> event) {
        var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, event); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
        monitor.debug("Received provider finalize: " + providerId);
        var consumerId = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, event); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
        var negotiation = findById(consumerId);
        negotiation.transition(ContractNegotiation.State.FINALIZED, n -> listeners.forEach(l -> l.finalized(negotiation)));
    }

    @Override
    public Map<String, ContractNegotiation> getNegotiations() {
        return negotiations;
    }

    @Override
    public void registerListener(NegotiationListener listener) {
        listeners.add(listener);
    }

    @Override
    public void deregisterListener(NegotiationListener listener) {
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
