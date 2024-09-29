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

/**
 * Mock service for recording provider connector actions.
 */
public interface ProviderNegotiationMock extends NegotiationMock {

    /**
     * Records an action to execute when a contract request is processed on the provider.
     */
    void recordContractRequestedAction(Action action);

    /**
     * Records an action to execute when a consumer agrees to an offer.
     */
    void recordAgreedAction(Action action);

    /**
     * Records an action to execute when a consumer verifies an offer.
     */
    void recordVerifiedAction(Action action);


}
