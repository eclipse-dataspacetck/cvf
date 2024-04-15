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

package org.eclipse.dataspacetck.dsp.system.api.message;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.CONTEXT;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSP_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSP_NAMESPACE_KEY;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSP_NAMESPACE_PREFIX;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ODRL_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TYPE;

/**
 * Utility methods for creating DSP messages.
 */
public class MessageFunctions {

    public static Map<String, Object> createContractRequest(String processId, String offerId, String datasetId, String callbackAddress) {
        var message = createBaseMessage(DSP_NAMESPACE_PREFIX + "ContractRequestMessage");
        message.put(ID, processId); // override id
        message.put(CONTEXT, createContext());

        message.put(DSP_NAMESPACE_PREFIX + "offerId", offerId);
        message.put(DSP_NAMESPACE_PREFIX + "datasetId", datasetId);

        message.put(DSP_NAMESPACE_PREFIX + "callbackAddress", callbackAddress);

        return message;
    }

    public static Map<String, Object> createCounterOffer(String processId, String datasetId) {
        var message = createBaseMessage(DSP_NAMESPACE_PREFIX + "ContractRequestMessage"); // do NOT override id
        message.put(DSP_NAMESPACE_PREFIX + "processId", processId);
        message.put(CONTEXT, createContext());

        message.put(DSP_NAMESPACE_PREFIX + "offer", createOffer(processId, UUID.randomUUID().toString(), datasetId));
        message.put(DSP_NAMESPACE_PREFIX + "datasetId", datasetId);

        return message;
    }

    public static Map<String, Object> createTermination(String processId, String code, String... reasons) {
        var message = createBaseMessage(DSP_NAMESPACE_PREFIX + "ContractNegotiationTermination");
        message.put(CONTEXT, createContext());

        message.put(DSP_NAMESPACE_PREFIX + "processId", processId);
        message.put(DSP_NAMESPACE_PREFIX + "code", code);

        if (reasons != null && reasons.length > 0) {
            message.put(DSP_NAMESPACE_PREFIX + "reasons", Arrays.stream(reasons).map(reason -> Map.of("message", reason)).collect(toList()));
        }
        return message;
    }

    public static Map<String, Object> createAcceptedEvent(String processId) {
        return createEvent(processId, "accepted");
    }

    public static Map<String, Object> createFinalizedEvent(String processId) {
        return createEvent(processId, "finalized");
    }

    public static Map<String, Object> createEvent(String processId, String eventType) {
        var message = createBaseMessage(DSP_NAMESPACE_PREFIX + "ContractNegotiationEventMessage");
        message.put(CONTEXT, createContext());

        message.put(DSP_NAMESPACE_PREFIX + "processId", processId);
        message.put(DSP_NAMESPACE_PREFIX + "eventType", eventType);
        return message;
    }

    public static Map<String, Object> createVerification(String processId) {
        var message = createBaseMessage(DSP_NAMESPACE_PREFIX + "ContractAgreementVerificationMessage");
        message.put(CONTEXT, createContext());

        message.put(DSP_NAMESPACE_PREFIX + "processId", processId);
        return message;
    }

    public static Map<String, Object> createOffer(String processId, String offerId, String datasetId) {
        var message = createBaseMessage(DSP_NAMESPACE_PREFIX + "ContractOfferMessage");
        var context = createContext();
        context.put("odrl", ODRL_NAMESPACE);
        message.put(CONTEXT, context);
        message.put(DSP_NAMESPACE_PREFIX + "processId", processId);

        var permissions = Map.of("action", "use", "constraints", emptyList());
        var offer = new LinkedHashMap<String, Object>();
        offer.put(TYPE, "odrl:Offer");
        offer.put(ID, offerId);
        offer.put(DSP_NAMESPACE_PREFIX + "target", datasetId);
        offer.put(DSP_NAMESPACE_PREFIX + "permissions", List.of(permissions));

        message.put(DSP_NAMESPACE_PREFIX + "offer", offer);

        return message;
    }

    public static Map<String, Object> createAgreement(String processId, String agreementId, String datasetId) {
        var message = createBaseMessage(DSP_NAMESPACE_PREFIX + "ContractAgreementMessage");
        var context = createContext();
        context.put("odrl", ODRL_NAMESPACE);
        message.put(CONTEXT, context);
        message.put(DSP_NAMESPACE_PREFIX + "processId", processId);

        var permissions = Map.of("action", "use", "constraints", emptyList());
        var offer = new LinkedHashMap<String, Object>();
        offer.put(TYPE, "odrl:Agreement");
        offer.put(ID, agreementId);
        offer.put(DSP_NAMESPACE_PREFIX + "target", datasetId);
        offer.put(DSP_NAMESPACE_PREFIX + "permissions", List.of(permissions));

        message.put(DSP_NAMESPACE_PREFIX + "agreement", offer);

        return message;
    }

    public static Map<String, Object> createNegotiationResponse(String id, String state) {
        var message = createBaseMessage(DSP_NAMESPACE_PREFIX + "ContractNegotiation");
        var context = createContext();
        message.put(CONTEXT, context);
        message.put(ID, id);
        message.put(DSP_NAMESPACE_PREFIX + "state", state);  // TODO JSON-LD
        return message;
    }

    public static Map<String, Object> mapProperty(String key, Map<String, Object> map) {
        var value = requireNonNull(map.get(key), "No value for: " + key);
        //noinspection unchecked
        return (Map<String, Object>) value;
    }

    public static String stringProperty(String key, Map<String, Object> map) {
        var value = requireNonNull(map.get(key), "No value for: " + key);
        return (String) value;
    }

    @NotNull
    private static Map<String, Object> createBaseMessage(String type) {
        var message = new LinkedHashMap<String, Object>();
        message.put(ID, UUID.randomUUID().toString());
        message.put(TYPE, type);
        return message;
    }

    private static Map<String, Object> createContext() {
        var context = new LinkedHashMap<String, Object>();
        context.put(DSP_NAMESPACE_KEY, DSP_NAMESPACE);
        return context;
    }

    private MessageFunctions() {
    }
}
