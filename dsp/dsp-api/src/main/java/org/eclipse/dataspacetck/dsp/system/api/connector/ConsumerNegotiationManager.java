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

import java.util.Map;

/**
 * Manages contract negotiations on a consumer.
 */
public interface ConsumerNegotiationManager extends NegotiationManager {

    /**
     * Creates a contract negotiation for the given dataset.
     */
    ContractNegotiation createNegotiation(String datasetId, String offerId);

    /**
     * Called after a contract has been requested and the negotiation id is returned by the provider. The provider negotiation
     * id will be set as the correlation id on the consumer.
     */
    void contractRequested(String consumerId, String providerId);

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#REQUESTED} when a counter-offer has been made.
     */
    void counterOffered(String consumerId);

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#ACCEPTED} when an offer is accepted by the consumer.
     */
    void accepted(String consumerId);

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#VERIFIED} when a verification is being sent.
     */
    void verified(String consumerId);

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#TERMINATED}.
     */
    void terminated(String consumerId);

    /**
     * Processes an offer received from the provider.
     */
    Map<String, Object> handleOffer(Map<String, Object> offer);

    /**
     * Processes an agreement received from the provider.
     */
    void handleAgreement(Map<String, Object> agreement);

    /**
     * Processes a finalize event received from the provider.
     */
    void handleFinalized(Map<String, Object> event);

}
