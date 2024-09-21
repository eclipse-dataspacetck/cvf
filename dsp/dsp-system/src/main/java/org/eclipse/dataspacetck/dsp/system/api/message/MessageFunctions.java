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

import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.CONTEXT;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE_KEY;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE_PREFIX;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CALLBACK_ADDRESS;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CODE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_EVENT_TYPE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_OFFER;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_REASON;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TYPE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.VALUE;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_AGREEMENT_TYPE;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_NAMESPACE_KEY;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_OFFER_TYPE;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_PROPERTY_ACTION;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_PROPERTY_CONSTRAINTS;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_PROPERTY_PERMISSION;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_PROPERTY_TARGET;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_USE;

/**
 * Utility methods for creating DSP messages.
 */
public class MessageFunctions {
    private static final Map<String, String> IDENTITY_TYPE = Map.of("@type", "@id");

    public static Map<String, Object> createContractRequest(String consumerPid, String offerId, String targetId, String callbackAddress) {
        var message = createBaseMessage(DSPACE_NAMESPACE_PREFIX + "ContractRequestMessage");
        message.put(CONTEXT, createDspContext());
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerPid);

        var offer = new LinkedHashMap<String, Object>();
        offer.put(ID, offerId);
        offer.put(ODRL_PROPERTY_TARGET, targetId);
        offer.put(TYPE, ODRL_NAMESPACE + "Offer");    // WORKAROUND: REMOVE - @type

        message.put(DSPACE_PROPERTY_OFFER, offer);

        message.put(DSPACE_PROPERTY_CALLBACK_ADDRESS, callbackAddress);

        return message;
    }

    public static Map<String, Object> createCounterOffer(String providerId,
                                                         String consumerId,
                                                         String offerId,
                                                         String targetId,
                                                         String callbackAddress) {
        var message = createBaseMessage(DSPACE_NAMESPACE_PREFIX + "ContractRequestMessage"); // do NOT override id
        message.put(CONTEXT, createDspContext());
        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerId);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerId);

        message.put(DSPACE_PROPERTY_OFFER, createOfferPolicy(offerId, targetId));
        message.put(DSPACE_PROPERTY_CALLBACK_ADDRESS, callbackAddress);

        return message;
    }

    public static Map<String, Object> createTermination(String providerId, String consumerId, String code, String... reasons) {
        var message = createBaseMessage(DSPACE_NAMESPACE_PREFIX + "ContractNegotiationTerminationMessage");
        message.put(CONTEXT, createDspContext());

        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerId);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerId);
        message.put(DSPACE_PROPERTY_CODE, code);

        if (reasons != null && reasons.length > 0) {
            message.put(DSPACE_PROPERTY_REASON, Arrays.stream(reasons).map(reason -> Map.of("message", reason)).collect(toList()));
        }
        return message;
    }

    public static Map<String, Object> createAcceptedEvent(String processId, String consumerId) {
        return createEvent(processId, consumerId, "ACCEPTED");
    }

    public static Map<String, Object> createFinalizedEvent(String processId, String consumerId) {
        return createEvent(processId, consumerId, "FINALIZED");
    }

    public static Map<String, Object> createEvent(String providerId, String consumerId, String eventType) {
        var message = createBaseMessage(DSPACE_NAMESPACE_PREFIX + "ContractNegotiationEventMessage");
        message.put(CONTEXT, createDspContext());
        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerId);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerId);

        message.put(DSPACE_PROPERTY_EVENT_TYPE, DSPACE_NAMESPACE + eventType);
        return message;
    }

    public static Map<String, Object> createVerification(String providerId, String consumerId) {
        var message = createBaseMessage(DSPACE_NAMESPACE_PREFIX + "ContractAgreementVerificationMessage");
        message.put(CONTEXT, createDspContext());

        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerId);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerId);
        return message;
    }

    public static Map<String, Object> createOffer(String providerId, String consumerId, String offerId, String targetId) {
        var message = createBaseMessage(DSPACE_NAMESPACE_PREFIX + "ContractOfferMessage");
        var context = createDspContext();
        message.put(CONTEXT, context);
        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerId);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerId);

        var offer = createOfferPolicy(offerId, targetId);

        message.put(DSPACE_PROPERTY_OFFER, offer);

        return message;
    }

    @NotNull
    private static LinkedHashMap<String, Object> createOfferPolicy(String offerId, String targetId) {
        var offer = new LinkedHashMap<String, Object>();
        offer.put(TYPE, ODRL_OFFER_TYPE); // WORKAROUND: REMOVE @type
        offer.put(ID, offerId);
        var permissions = Map.of(ODRL_PROPERTY_ACTION, ODRL_USE, ODRL_PROPERTY_CONSTRAINTS, emptyList());
        offer.put(ODRL_PROPERTY_PERMISSION, List.of(permissions));
        offer.put(ODRL_PROPERTY_TARGET, targetId);
        return offer;
    }

    public static Map<String, Object> createAgreement(String providerId, String consumerId, String agreementId, String target) {
        var message = createBaseMessage(DSPACE_NAMESPACE_PREFIX + "ContractAgreementMessage");
        var context = createDspContext();
        message.put(CONTEXT, context);
        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerId);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerId);

        var permissions = Map.of(ODRL_PROPERTY_ACTION, ODRL_USE, ODRL_PROPERTY_CONSTRAINTS, emptyList());
        var offer = new LinkedHashMap<String, Object>();
        offer.put(TYPE, ODRL_AGREEMENT_TYPE);
        offer.put(ID, agreementId);
        offer.put(ODRL_PROPERTY_TARGET, target);
        offer.put(ODRL_PROPERTY_PERMISSION, List.of(permissions));

        message.put(DSPACE_NAMESPACE_PREFIX + "agreement", offer);

        return message;
    }

    public static Map<String, Object> createNegotiationResponse(String providerPid, String consumerPid, String state) {
        var message = createBaseMessage(DSPACE_NAMESPACE_PREFIX + "ContractNegotiation");
        var context = createDspContext();
        message.put(CONTEXT, context);
        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerPid);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerPid);
        message.put(DSPACE_PROPERTY_STATE, state);
        return message;
    }

    public static Map<String, Object> createOfferAck(String providerId, String consumerId, State state) {
        var message = createBaseMessage(DSPACE_NAMESPACE_PREFIX + "ContractNegotiation");
        var context = createDspContext();
        message.put(CONTEXT, context);
        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerId);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerId);
        message.put(DSPACE_PROPERTY_STATE, state.toString());
        return message;
    }

    public static Map<String, Object> mapProperty(String key, Map<String, Object> map) {
        var untypedValue = requireNonNull(map.get(key), "No value for: " + key);
        //noinspection rawtypes
        if (untypedValue instanceof List valueList) {
            if (valueList.isEmpty()) {
                throw new AssertionError(format("Property '%s' was empty", key));
            }
            @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
            var valueContainer = valueList.get(0);
            if (valueContainer instanceof Map) {
                return map;
            }
        }
        throw new AssertionError(format("Property '%s' is not a Map", key));
    }

    public static String compactStringProperty(String key, Map<String, Object> map) {
        var value = requireNonNull(map.get(key), "No value for: " + key);
        return (String) value;
    }

    public static String stringProperty(String key, Map<String, Object> map) {
        return stringProperty(key, VALUE, map);
    }

    public static String stringIdProperty(String key, Map<String, Object> map) {
        return stringProperty(key, ID, map);
    }

    public static String stringProperty(String key, String valKey, Map<String, Object> map) {
        var untypedValue = requireNonNull(map.get(key), "No value for: " + key);
        //noinspection rawtypes
        if (untypedValue instanceof List valueList) {
            if (valueList.isEmpty()) {
                throw new AssertionError(format("Property '%s' was empty", key));
            }
            @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
            var valueContainer = valueList.get(0);
            if (valueContainer instanceof Map) {
                @SuppressWarnings("rawtypes")
                var value = requireNonNull(((Map) valueContainer).get(valKey), format("No %s attribute for property: %s", valKey, key));
                return value.toString();
            }
        }
        throw new AssertionError(format("Property '%s' was not in expanded @value form", key));
    }

    public static String identityProperty(String key, Map<String, Object> map) {
        var value = requireNonNull(map.get(key), "No value for: " + key);
        if (value instanceof Map) {
            @SuppressWarnings("rawtypes")
            var idValue = requireNonNull(((Map) value).get(ID), "No @id value for property: " + key);
            return idValue.toString();
        }
        throw new AssertionError(format("Property '%s' was not in expanded @id form", key));
    }

    public static Map<String, Object> createDspContext() {
        var context = new LinkedHashMap<String, Object>();
        context.put(DSPACE_NAMESPACE_KEY, DSPACE_NAMESPACE);
        context.put(ODRL_NAMESPACE_KEY, ODRL_NAMESPACE);
        context.put("target", "odrl:target");
        context.put("odrl:target", IDENTITY_TYPE);
        context.put(DSPACE_NAMESPACE_PREFIX + "state", IDENTITY_TYPE);
        context.put(DSPACE_NAMESPACE_PREFIX + "consumerPid", IDENTITY_TYPE);
        context.put(DSPACE_NAMESPACE_PREFIX + "providerPid", IDENTITY_TYPE);
        return context;
    }

    @NotNull
    private static Map<String, Object> createBaseMessage(String type) {
        var message = new LinkedHashMap<String, Object>();
        message.put(ID, UUID.randomUUID().toString());
        message.put(TYPE, type);
        return message;
    }

    private MessageFunctions() {
    }

}
