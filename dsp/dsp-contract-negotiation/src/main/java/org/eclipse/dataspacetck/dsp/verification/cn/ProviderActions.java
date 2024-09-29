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

package org.eclipse.dataspacetck.dsp.verification.cn;

import okhttp3.Response;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.eclipse.dataspacetck.dsp.system.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.message.MessageFunctions.createAgreement;
import static org.eclipse.dataspacetck.dsp.system.message.MessageFunctions.createFinalizedEvent;
import static org.eclipse.dataspacetck.dsp.system.message.MessageFunctions.createOffer;
import static org.eclipse.dataspacetck.dsp.system.message.MessageFunctions.createTermination;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.AGREED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.FINALIZED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;

/**
 * Actions taken by a provider that execute after receiving a message from the consumer.
 */
public class ProviderActions {
    private static final String NEGOTIATION_OFFER_TEMPLATE = "%s/negotiations/%s/offer/";
    private static final String NEGOTIATION_TERMINATE_TEMPLATE = "%s/negotiations/%s/termination/";
    private static final String NEGOTIATION_AGREEMENT_TEMPLATE = "%s/negotiations/%s/agreement";
    private static final String NEGOTIATION_FINALIZE_TEMPLATE = "%s/negotiations/%s/events";

    public static void postOffer(ContractNegotiation negotiation) {
        var contractOffer = createOffer(negotiation.getId(), negotiation.getCorrelationId(), randomUUID().toString(), randomUUID().toString());

        negotiation.transition(OFFERED);
        try (var response = postJson(format(NEGOTIATION_OFFER_TEMPLATE, negotiation.getCallbackAddress(), negotiation.getCorrelationId()), contractOffer)) {
            checkResponse(response);
        }
    }

    public static void postAgreed(ContractNegotiation negotiation) {
        var agreement = createAgreement(negotiation.getId(), negotiation.getCorrelationId(), randomUUID().toString(), negotiation.getDatasetId());

        negotiation.transition(AGREED);
        try (var response = postJson(format(NEGOTIATION_AGREEMENT_TEMPLATE, negotiation.getCallbackAddress(), negotiation.getCorrelationId()), agreement)) {
            checkResponse(response);
        }
    }

    public static void postFinalized(ContractNegotiation negotiation) {
        negotiation.transition(FINALIZED);
        var event = createFinalizedEvent(negotiation.getId(), negotiation.getCorrelationId());
        try (var response = postJson(format(NEGOTIATION_FINALIZE_TEMPLATE, negotiation.getCallbackAddress(), negotiation.getCorrelationId()), event)) {
            checkResponse(response);
        }
    }

    public static void postTerminate(ContractNegotiation negotiation) {
        var termination = createTermination(negotiation.getId(), negotiation.getCorrelationId(), "1");
        try (var response = postJson(format(NEGOTIATION_TERMINATE_TEMPLATE, negotiation.getCallbackAddress(), negotiation.getCorrelationId()), termination)) {
            checkResponse(response);
        }
    }

    public static void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkResponse(Response response) {
        if (!response.isSuccessful()) {
            throw new AssertionError("Unexpected response code: " + response.code());
        }
    }

    private ProviderActions() {
    }
}
