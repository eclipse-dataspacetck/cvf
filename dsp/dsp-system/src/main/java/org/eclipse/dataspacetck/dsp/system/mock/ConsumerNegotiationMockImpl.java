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

import org.eclipse.dataspacetck.dsp.system.api.connector.ConsumerNegotiationManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.NegotiationListener;
import org.eclipse.dataspacetck.dsp.system.api.mock.ConsumerNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.AGREED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.FINALIZED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.INITIALIZED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.REQUESTED;

/**
 * Default mock consumer implementation.
 */
public class ConsumerNegotiationMockImpl extends AbstractNegotiationMock implements ConsumerNegotiationMock, NegotiationListener {
    private ConsumerNegotiationManager manager;
    private String baseAddress;

    public ConsumerNegotiationMockImpl(ConsumerNegotiationManager manager, Executor executor, String baseAddress) {
        super(executor);
        this.manager = manager;
        this.baseAddress = baseAddress;
        manager.registerListener(this);
    }

    @Override
    public void recordInitializedAction(BiConsumer<String, ContractNegotiation> action) {
        recordAction(INITIALIZED, cn -> {
            action.accept(baseAddress, cn);
        });
    }

    @Override
    public void recordRequestAction(BiConsumer<String, ContractNegotiation> action) {
        recordAction(REQUESTED, cn -> {
            action.accept(baseAddress, cn);
        });
    }

    @Override
    public void recordOfferedAction(BiConsumer<String, ContractNegotiation> action) {
        recordAction(OFFERED, cn -> {
            cn.transition(OFFERED);
            action.accept(baseAddress, cn);
        });
    }

    @Override
    public void recordAgreedAction(BiConsumer<String, ContractNegotiation> action) {
        recordAction(AGREED, cn -> action.accept(baseAddress, cn));
    }

    @Override
    public void contractInitialized(ContractNegotiation negotiation) {
        received(INITIALIZED, negotiation);
    }

    @Override
    public void contractRequested(ContractNegotiation negotiation) {
        received(REQUESTED, negotiation);
    }

    @Override
    public void offered(ContractNegotiation negotiation) {
        received(OFFERED, negotiation);
    }

    @Override
    public void agreed(ContractNegotiation negotiation) {
        received(AGREED, negotiation);
    }

    @Override
    public void finalized(ContractNegotiation negotiation) {
        received(FINALIZED, negotiation);
    }

    @Override
    public void verify() {
        super.verify();
        manager.deregisterListener(this);
    }

    private void received(ContractNegotiation.State state, ContractNegotiation negotiation) {
        var action = actions.getOrDefault(state, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(negotiation));
    }

}
