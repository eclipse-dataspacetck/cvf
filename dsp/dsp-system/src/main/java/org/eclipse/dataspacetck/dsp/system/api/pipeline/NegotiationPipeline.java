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

package org.eclipse.dataspacetck.dsp.system.api.pipeline;

import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State;

import java.util.concurrent.Callable;

/**
 * Drives message interactions with a connector under test. Uses a TCK connector to interact with the connector being verified.
 */
public interface NegotiationPipeline<P extends NegotiationPipeline<P>> {

    /**
     * Waits for the transition to the given state.
     */
    P thenWaitForState(State state);

    /**
     * Waits for the condition.
     */
    P thenWait(String description, Callable<Boolean> condition);

    /**
     * Executes the pipeline actions.
     */
    void execute();

}
