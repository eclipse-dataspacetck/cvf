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

package cvf.ids.system.api.connector;

import cvf.ids.system.api.statemachine.ContractNegotiation;

/**
 * Implementations can be registered to receive contract negotiation events for a consumer.
 */
public interface ConsumerNegotiationListener extends NegotiationListener {

    /**
     * Invoked when a contract negotiation is created.
     */
    void negotiationCreated(ContractNegotiation negotiation);

}
