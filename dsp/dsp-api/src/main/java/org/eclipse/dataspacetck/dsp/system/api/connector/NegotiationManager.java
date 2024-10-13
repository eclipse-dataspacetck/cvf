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

package org.eclipse.dataspacetck.dsp.system.api.connector;

import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Manages contract negotiations. Subclasses implement specific behavior for consumer and provider state transitions.
 */
public interface NegotiationManager {

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
