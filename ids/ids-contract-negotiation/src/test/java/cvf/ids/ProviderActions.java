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

package cvf.ids;

import cvf.ids.system.api.statemachine.ContractNegotiation;
import okhttp3.Response;

import static cvf.ids.system.api.http.HttpFunctions.postJson;
import static cvf.ids.system.api.message.MessageFunctions.createAgreement;
import static cvf.ids.system.api.message.MessageFunctions.createFinalizedEvent;
import static cvf.ids.system.api.message.MessageFunctions.createOffer;
import static cvf.ids.system.api.message.MessageFunctions.createTermination;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_AGREED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_FINALIZED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_OFFERED;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

/**
 * Actions taken by a provider that execute after receiving a message from the client.
 */
public class ProviderActions {
    private static final String NEGOTIATION_OFFER_TEMPLATE = "%s/negotiations/%s/offer/";
    private static final String NEGOTIATION_TERMINATE_TEMPLATE = "%s/negotiations/%s/termination/";
    private static final String NEGOTIATION_AGREEMENT_TEMPLATE = "%s/negotiations/%s/agreement";
    private static final String NEGOTIATION_FINALIZE_TEMPLATE = "%s/negotiations/%s/events";

    public static void postOffer(ContractNegotiation negotiation) {
        var contractOffer = createOffer(negotiation.getId(), randomUUID().toString(), negotiation.getDatasetId());

        negotiation.transition(PROVIDER_OFFERED);
        try (var response = postJson(format(NEGOTIATION_OFFER_TEMPLATE, negotiation.getCallbackAddress(), negotiation.getCorrelationId()), contractOffer)) {
            checkResponse(response);
        }
    }

    public static void postProviderAgreed(ContractNegotiation negotiation) {
        var agreement = createAgreement(negotiation.getId(), randomUUID().toString(), negotiation.getDatasetId());

        negotiation.transition(PROVIDER_AGREED);
        try (var response = postJson(format(NEGOTIATION_AGREEMENT_TEMPLATE, negotiation.getCallbackAddress(), negotiation.getCorrelationId()), agreement)) {
            checkResponse(response);
        }
    }

    public static void postProviderFinalized(ContractNegotiation negotiation) {
        negotiation.transition(PROVIDER_FINALIZED);
        var event = createFinalizedEvent(negotiation.getId());
        try (var response = postJson(format(NEGOTIATION_FINALIZE_TEMPLATE, negotiation.getCallbackAddress(), negotiation.getCorrelationId()), event)) {
            checkResponse(response);
        }
    }

    public static void terminate(ContractNegotiation negotiation) {
        var termination = createTermination(negotiation.getId(), "1");
        try (var response = postJson(format(NEGOTIATION_TERMINATE_TEMPLATE, negotiation.getCallbackAddress(), negotiation.getCorrelationId()), termination)) {
            checkResponse(response);
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
