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

/**
 * Manages contract negotiations on a provider.
 */
public interface ProviderNegotiationManager {

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#OFFERED} state.
     */
    void offered(String providerId);

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#AGREED} state.
     */
    void agreed(String providerId);

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#FINALIZED} state.
     */
    void finalized(String providerId);

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#FINALIZED} state.
     */
    void terminated(Map<String, Object> termination);

    /**
     * Called when a contract request is received.
     */
    ContractNegotiation handleContractRequest(Map<String, Object> contractRequest);

    /**
     * Processes an agreed event received from the consumer.
     */
    void handleAgreed(Map<String, Object> event);

    /**
     * Processes a verification received from the consumer.
     */
    void handleVerified(Map<String, Object> verification);

    /**
     * Returns a negotiation by id or throws an {@code IllegalArgumentException} if not found.
     */
    @NotNull
    ContractNegotiation findById(String id);

    /**
     * Returns a negotiation by correlation id or null if not found.
     */
    @Nullable
    ContractNegotiation findByCorrelationId(String id);

    /**
     * Returns all negotiations.
     */
    Map<String, ContractNegotiation> getNegotiations();

    /**
     * Registers a listener.
     */
    void registerListener(NegotiationListener listener);

    /**
     * Removes a listener.
     */
    void deregisterListener(NegotiationListener listener);

}
