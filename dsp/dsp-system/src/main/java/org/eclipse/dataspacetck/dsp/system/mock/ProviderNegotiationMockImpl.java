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

import org.eclipse.dataspacetck.dsp.system.api.connector.NegotiationListener;
import org.eclipse.dataspacetck.dsp.system.api.connector.ProviderNegotiationManager;
import org.eclipse.dataspacetck.dsp.system.api.mock.ProviderNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.concurrent.Executor;

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.ACCEPTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.VERIFIED;

/**
 * Default mock provider implementation.
 */
public class ProviderNegotiationMockImpl extends AbstractNegotiationMock implements ProviderNegotiationMock, NegotiationListener {
    private ProviderNegotiationManager manager;

    public ProviderNegotiationMockImpl(ProviderNegotiationManager manager, Executor executor) {
        super(executor);
        this.manager = manager;
        manager.registerListener(this);
    }

    @Override
    public void recordContractRequestedAction(Action action) {
        recordAction(REQUESTED, action);
    }

    @Override
    public void recordAgreedAction(Action action) {
        recordAction(ACCEPTED, action);
    }

    @Override
    public void recordVerifiedAction(Action action) {
        recordAction(VERIFIED, action);
    }

    public void verify() {
        super.verify();
        manager.deregisterListener(this);
    }

    @Override
    public void contractRequested(ContractNegotiation negotiation) {
        var action = actions.getOrDefault(REQUESTED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(negotiation));
    }

    @Override
    public void agreed(ContractNegotiation negotiation) {
        var action = actions.getOrDefault(ACCEPTED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(negotiation));
    }

    @Override
    public void verified(ContractNegotiation negotiation) {
        var action = actions.getOrDefault(VERIFIED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(negotiation));
    }

}
