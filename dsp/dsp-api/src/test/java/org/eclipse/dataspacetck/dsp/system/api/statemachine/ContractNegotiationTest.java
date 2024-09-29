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

package org.eclipse.dataspacetck.dsp.system.api.statemachine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.function.BiConsumer;

import static java.util.UUID.randomUUID;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.INITIALIZED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.REQUESTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ContractNegotiationTest {
    ContractNegotiation negotiation;

    @ParameterizedTest
    @EnumSource(value = ContractNegotiation.State.class, names = {"TERMINATED", "REQUESTED", "OFFERED" })
    void verifyValidInitialStateTransitions(ContractNegotiation.State state) {
        if (REQUESTED == state || OFFERED == state) {
            negotiation.setCorrelationId(randomUUID().toString(), state);
        } else {
            negotiation.transition(state);
        }
        assertEquals(state, negotiation.getState());
    }

    @ParameterizedTest
    @EnumSource(value = ContractNegotiation.State.class, names = { "REQUESTED", "OFFERED" })
    void verifyInvalidValidCorrelationIdTransitions(ContractNegotiation.State state) {
        assertThrows(IllegalStateException.class, () -> negotiation.transition(state));
    }

    @ParameterizedTest
    @EnumSource(value = ContractNegotiation.State.class, names = {"INITIALIZED", "ACCEPTED", "AGREED", "VERIFIED", "FINALIZED" })
    void verifyInValidInitialStateTransitions(ContractNegotiation.State state) {
        assertThrows(IllegalStateException.class, () -> negotiation.transition(state));
    }

    @Test
    void verifyListenerCalled() {
        @SuppressWarnings("unchecked") BiConsumer<ContractNegotiation.State, ContractNegotiation> listener = mock(BiConsumer.class);
        negotiation = ContractNegotiation.Builder.newInstance().datasetId(randomUUID().toString()).correlationId(randomUUID().toString()).listener(listener).build();
        negotiation.transition(REQUESTED);

        verify(listener, times(1)).accept(eq(INITIALIZED), isA(ContractNegotiation.class));
    }

    @BeforeEach
    void setUp() {
        negotiation = ContractNegotiation.Builder.newInstance().datasetId(randomUUID().toString()).build();
    }
}
