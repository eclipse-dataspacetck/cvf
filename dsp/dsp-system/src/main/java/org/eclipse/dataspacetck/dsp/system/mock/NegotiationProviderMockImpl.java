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

package org.eclipse.dataspacetck.dsp.system.mock;

import org.eclipse.dataspacetck.dsp.system.api.connector.ProviderNegotiationListener;
import org.eclipse.dataspacetck.dsp.system.api.connector.ProviderNegotiationManager;
import org.eclipse.dataspacetck.dsp.system.api.mock.NegotiationProviderMock;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

/**
 * Default mock implementation.
 */
public class NegotiationProviderMockImpl implements NegotiationProviderMock, ProviderNegotiationListener {
    private ProviderNegotiationManager manager;
    private Executor executor;
    private static final Queue<Action> EMPTY_QUEUE = new ArrayDeque<>();

    private Map<ContractNegotiation.State, Queue<Action>> actions = new ConcurrentHashMap<>();

    public NegotiationProviderMockImpl(ProviderNegotiationManager manager, Executor executor) {
        this.manager = manager;
        this.executor = executor;
        manager.registerListener(this);
    }

    @Override
    public void recordContractRequestedAction(Action action) {
        recordAction(ContractNegotiation.State.REQUESTED, action);
    }

    @Override
    public void recordConsumerAgreedAction(Action action) {
        recordAction(ContractNegotiation.State.ACCEPTED, action);
    }

    @Override
    public void recordConsumerVerifyAction(Action action) {
        recordAction(ContractNegotiation.State.VERIFIED, action);
    }

    @Override
    public void contractRequested(Map<String, Object> contractRequest, ContractNegotiation negotiation) {
        var action = actions.getOrDefault(ContractNegotiation.State.REQUESTED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(negotiation));
    }

    @Override
    public void consumerAgreed(ContractNegotiation negotiation) {
        var action = actions.getOrDefault(ContractNegotiation.State.ACCEPTED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(negotiation));
    }

    @Override
    public void consumerVerified(Map<String, Object> verification, ContractNegotiation negotiation) {
        var action = actions.getOrDefault(ContractNegotiation.State.VERIFIED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(negotiation));
    }

    public void verify() {
        if (!actions.isEmpty()) {
            var actions = this.actions.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .map(e -> e.getKey().toString())
                    .collect(toList());
            if (!actions.isEmpty()) {
                throw new AssertionError(format("Request actions not executed.\n Actions: %s", join(", ", actions)));
            }
        }
        manager.deregisterListener(this);
    }

    @Override
    public boolean completed() {
        return actions.isEmpty();
    }

    @Override
    public void reset() {
        actions.clear();
    }

    private boolean recordAction(ContractNegotiation.State state, Action action) {
        return actions.computeIfAbsent(state, k -> new ConcurrentLinkedQueue<>()).add(action);
    }

}
