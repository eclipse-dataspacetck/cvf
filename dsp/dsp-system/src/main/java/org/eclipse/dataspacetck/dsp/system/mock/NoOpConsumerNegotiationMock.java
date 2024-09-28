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

import org.eclipse.dataspacetck.dsp.system.api.mock.ConsumerNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.function.BiConsumer;

/**
 * A no-op mock used when the local connector is disabled.
 */
public class NoOpConsumerNegotiationMock implements ConsumerNegotiationMock {

    @Override
    public void recordOfferedAction(BiConsumer<String, ContractNegotiation> action) {
    }

    @Override
    public void recordAgreedAction(BiConsumer<String, ContractNegotiation> action) {
    }

    @Override
    public void verify() {
    }

    @Override
    public boolean completed() {
        return false;
    }

    @Override
    public void reset() {
    }

}
