/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.dsp.system.mock;

import org.eclipse.dataspacetck.dsp.system.api.mock.ProviderNegotiationMock;

/**
 * A no-op mock used when the local connector is disabled.
 */
public class NoOpProviderNegotiationMock implements ProviderNegotiationMock {

    @Override
    public void recordContractRequestedAction(Action action) {
    }

    @Override
    public void recordAgreedAction(Action action) {
    }

    @Override
    public void recordVerifiedAction(Action action) {
    }

    @Override
    public void verify() {
    }

    @Override
    public boolean completed() {
        return true;
    }

    @Override
    public void reset() {

    }
}
