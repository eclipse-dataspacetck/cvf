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
package org.eclipse.dataspacetck.dsp.system.pipeline;

import org.eclipse.dataspacetck.core.api.pipeline.AbstractAsyncPipeline;
import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.NegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

/**
 * Base negotiation pipeline functionality.
 */
public abstract class AbstractNegotiationPipeline<P extends NegotiationPipeline<P>> extends AbstractAsyncPipeline<P> {
    protected ContractNegotiation negotiation;

    public AbstractNegotiationPipeline(CallbackEndpoint endpoint, Monitor monitor, long waitTime) {
        super(endpoint, monitor, waitTime, MessageFunctions::createDspContext);
    }

    public P thenWaitForState(ContractNegotiation.State state) {
        return thenWait("state to transition to " + state, () -> state == negotiation.getState());
    }


}
