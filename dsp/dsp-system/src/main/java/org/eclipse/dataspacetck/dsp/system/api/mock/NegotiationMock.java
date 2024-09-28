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

package org.eclipse.dataspacetck.dsp.system.api.mock;

import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.function.Consumer;

/**
 * Mock service for recording connector actions.
 */
public interface NegotiationMock {

    /**
     * An action to be executed by the mock.
     */
    interface Action extends Consumer<ContractNegotiation> {
    }

    /**
     * Verifies all actions have been executed.
     */
    void verify();

    /**
     * Returns true if all actions have been executed.
     */
    boolean completed();

    /**
     * Resets the mock and all recorded actions.
     */
    void reset();

}
