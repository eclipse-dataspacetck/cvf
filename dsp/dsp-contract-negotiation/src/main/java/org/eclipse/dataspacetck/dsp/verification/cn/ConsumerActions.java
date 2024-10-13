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
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createAcceptedEvent;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createContractRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createDspContext;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createVerification;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.ACCEPTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.VERIFIED;

/**
 * Actions taken by a consumer that execute after receiving a message from the provider.
 */
public class ConsumerActions {
    private static final String EVENT_PATH = "%s/negotiations/%s/events";
    private static final String REQUEST_PATH = "%s/negotiations/request";
    private static final String VERIFICATION_PATH = "%s/negotiations/%s/agreement/verification";

    public static void postRequest(String baseUrl, ContractNegotiation negotiation) {
        var url = format(REQUEST_PATH, baseUrl);
        var contractRequest = createContractRequest(negotiation.getId(), negotiation.getOfferId(), negotiation.getDatasetId(), baseUrl);
        try (var response = postJson(url, contractRequest)) {
            // get the response and update the negotiation with the provider process id
            checkResponse(response);
            assert response.body() != null;
            var jsonResponse = processJsonLd(response.body().byteStream(), createDspContext());
            var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, jsonResponse); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
            negotiation.setCorrelationId(providerId, REQUESTED);
        }
    }

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
        var url = format(VERIFICATION_PATH, baseUrl, negotiation.getCorrelationId());
        var verification = createVerification(negotiation.getCorrelationId(), negotiation.getId());
        try (var response = postJson(url, verification)) {
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
