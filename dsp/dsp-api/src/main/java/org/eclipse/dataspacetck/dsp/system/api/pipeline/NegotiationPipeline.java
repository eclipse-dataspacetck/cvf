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

import org.eclipse.dataspacetck.core.api.pipeline.AsyncPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State;

/**
 * Constructs a contract negotiation with a connector under test.
 */
public interface NegotiationPipeline<P extends NegotiationPipeline<P>> extends AsyncPipeline<P> {

    /**
     * Waits for the transition to the given state.
     */
    P thenWaitForState(State state);

}
