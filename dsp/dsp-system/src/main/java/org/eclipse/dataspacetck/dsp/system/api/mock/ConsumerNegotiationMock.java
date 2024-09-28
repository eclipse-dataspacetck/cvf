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

package org.eclipse.dataspacetck.dsp.system.api.mock;

import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.function.BiConsumer;

/**
 * Mock service for recording consumer connector actions.
 */
public interface ConsumerNegotiationMock extends NegotiationMock {

    void recordOfferedAction(BiConsumer<String, ContractNegotiation> action);

    void recordAgreedAction(BiConsumer<String, ContractNegotiation> action);
}
