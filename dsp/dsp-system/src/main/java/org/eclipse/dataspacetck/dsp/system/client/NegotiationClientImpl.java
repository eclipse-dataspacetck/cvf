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

package org.eclipse.dataspacetck.dsp.system.client;

import org.eclipse.dataspacetck.dsp.system.api.client.NegotiationClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.compact;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSP_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createNegotiationResponse;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringProperty;

/**
 * Default implementation that supports dispatch to a local, in-memory test connector or a remote connector system via HTTP.
 */
public class NegotiationClientImpl implements NegotiationClient {
    private Connector systemConnector;

    public NegotiationClientImpl() {
    }

    public NegotiationClientImpl(Connector systemConnector) {
        this.systemConnector = systemConnector;
    }

    @Override
    public Map<String, Object> contractRequest(Map<String, Object> contractRequest) {
        if (systemConnector != null) {
            var compacted = compact(contractRequest);
            var negotiation = systemConnector.getProviderNegotiationManager().handleContractRequest(compacted);
            return createNegotiationResponse(negotiation.getId(), negotiation.getState().toString().toLowerCase());
        }
        // TODO implement HTTP invoke
        return emptyMap();
    }

    @Override
    public void terminate(Map<String, Object> termination) {
        if (systemConnector != null) {
            var compacted = compact(termination);
            systemConnector.getProviderNegotiationManager().terminate(compacted);
        } else {
            // TODO implement HTTP invoke
        }
    }

    @Override
    public Map<String, Object> getNegotiation(String processId) {
        if (systemConnector != null) {
            var negotiation = systemConnector.getProviderNegotiationManager().findById(processId);
            return createNegotiationResponse(negotiation.getId(), negotiation.getState().toString().toLowerCase());
        }
        // TODO implement HTTP invoke
        return emptyMap();
    }

    @Override
    public void consumerAgree(Map<String, Object> event) {
        if (systemConnector != null) {
            var compacted = compact(event);
            var processId = stringProperty(DSP_NAMESPACE + "processId", compacted);
            systemConnector.getProviderNegotiationManager().handleConsumerAgreed(processId);
        }
        // TODO implement HTTP invoke
    }

    @Override
    public void consumerVerify(Map<String, Object> verification) {
        if (systemConnector != null) {
            var compacted = compact(verification);
            var processId = stringProperty(DSP_NAMESPACE + "processId", compacted);
            systemConnector.getProviderNegotiationManager().handleConsumerVerified(processId, verification);
        }
        // TODO implement HTTP invoke
    }
}
