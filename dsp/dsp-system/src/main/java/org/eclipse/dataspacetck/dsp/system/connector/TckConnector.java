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

package org.eclipse.dataspacetck.dsp.system.connector;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.ConsumerNegotiationManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.ProviderNegotiationManager;

/**
 * Implements a simple, in-memory connector that supports control-plane operations for testing.
 */
public class TckConnector implements Connector {
    private ProviderNegotiationManager providerNegotiationManager;
    private ConsumerNegotiationManager consumerNegotiationManager;

    public ProviderNegotiationManager getProviderNegotiationManager() {
        return providerNegotiationManager;
    }

    public ConsumerNegotiationManager getConsumerNegotiationManager() {
        return consumerNegotiationManager;
    }

    public TckConnector(Monitor monitor) {
        consumerNegotiationManager = new ConsumerNegotiationManagerImpl(monitor);
        providerNegotiationManager = new ProviderNegotiationManagerImpl();
    }
}
