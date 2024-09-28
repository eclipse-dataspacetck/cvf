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

package org.eclipse.dataspacetck.dsp.verification.cn;

import okhttp3.Response;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createAcceptedEvent;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.ACCEPTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.VERIFIED;

/**
 * Actions taken by a consumer that execute after receiving a message from the provider.
 */
public class ConsumerActions {
    private static final String EVENT_PATH = "%s/negotiations/%s/events";

    public static void postAccepted(String baseUrl, ContractNegotiation negotiation) {
        negotiation.transition(ACCEPTED);
        var url = format(EVENT_PATH, baseUrl, negotiation.getCorrelationId());
        var agreement = createAcceptedEvent(negotiation.getCorrelationId(), negotiation.getId());
        try (var response = postJson(url, agreement)) {
            checkResponse(response);
        }
    }

    public static void postVerification(String baseUrl, ContractNegotiation negotiation) {
        negotiation.transition(VERIFIED);
        var url = format(EVENT_PATH, baseUrl, negotiation.getCorrelationId());
        var agreement = createAcceptedEvent(negotiation.getCorrelationId(), negotiation.getId());
        try (var response = postJson(url, agreement)) {
            checkResponse(response);
        }
    }

    private static void checkResponse(Response response) {
        if (!response.isSuccessful()) {
            throw new AssertionError("Unexpected response code: " + response.code());
        }
    }

    private ConsumerActions() {
    }

}
