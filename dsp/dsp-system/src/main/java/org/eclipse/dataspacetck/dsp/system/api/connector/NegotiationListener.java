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

/**
 * Defines listener events for contract negotiations.
 */
public interface NegotiationListener {

    /**
     * Invoked when a contract request has been received.
     */
    default void contractRequested(ContractNegotiation negotiation) {
    }

    /**
     * Invoked when an offer has been received.
     */
    default void offered(ContractNegotiation negotiation) {
    }

    /**
     * Invoked after an agreement has been received.
     */
    default void agreed(ContractNegotiation negotiation) {
    }

    /**
     * Invoked after verified event has been received.
     */
    default void verified(ContractNegotiation negotiation) {
    }

    /**
     * Invoked when an agreement has been finalized.
     */
    default void finalized(ContractNegotiation negotiation) {
    }

    /**
     * Invoked when a termination has been received.
     */
    default void terminated(ContractNegotiation negotiation) {
    }

}
