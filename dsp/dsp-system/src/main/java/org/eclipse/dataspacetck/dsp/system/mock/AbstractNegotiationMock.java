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

package org.eclipse.dataspacetck.dsp.system.mock;

import org.eclipse.dataspacetck.dsp.system.api.mock.NegotiationMock;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.mockito.internal.util.StringUtil.join;

/**
 * Base negotiation mock functionality.
 */
public abstract class AbstractNegotiationMock implements NegotiationMock {
    protected Executor executor;
    protected static final Queue<Action> EMPTY_QUEUE = new ArrayDeque<>();

    protected Map<ContractNegotiation.State, Queue<Action>> actions = new ConcurrentHashMap<>();

    public AbstractNegotiationMock(Executor executor) {
        this.executor = executor;
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
    }

    @Override
    public boolean completed() {
        return actions.isEmpty();
    }

    @Override
    public void reset() {
        actions.clear();
    }

    protected void recordAction(ContractNegotiation.State state, Action action) {
        actions.computeIfAbsent(state, k -> new ConcurrentLinkedQueue<>()).add(action);
    }

}
