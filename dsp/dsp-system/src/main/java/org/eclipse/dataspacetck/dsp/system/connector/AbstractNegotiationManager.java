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

package org.eclipse.dataspacetck.dsp.system.connector;

import org.eclipse.dataspacetck.dsp.system.api.connector.NegotiationListener;
import org.eclipse.dataspacetck.dsp.system.api.connector.NegotiationManager;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Base implementation.
 */
public abstract class AbstractNegotiationManager implements NegotiationManager {
    protected Map<String, ContractNegotiation> negotiations = new ConcurrentHashMap<>();
    protected Queue<NegotiationListener> listeners = new ConcurrentLinkedQueue<>();

    @NotNull
    @Override
    public ContractNegotiation findById(String id) {
        var negotiation = negotiations.get(id);
        if (negotiation == null) {
            throw new IllegalArgumentException("Contract negotiation not found for id: " + id);
        }
        return negotiation;
    }

    @Nullable
    @Override
    public ContractNegotiation findByCorrelationId(String id) {
        return negotiations.values().stream()
                .filter(n -> id.equals(n.getCorrelationId()))
                .findAny().orElse(null);
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


}
