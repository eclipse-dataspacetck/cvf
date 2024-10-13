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

import org.eclipse.dataspacetck.dsp.system.api.connector.ProviderNegotiationManager;
import org.eclipse.dataspacetck.dsp.system.api.metadata.DcpSpecBug;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.eclipse.dataspacetck.dsp.system.api.connector.IdGenerator.datasetIdFromOfferId;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CALLBACK_ADDRESS_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_EVENT_TYPE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_OFFER_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.compactStringProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createNegotiationResponse;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.mapProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.stringProperty;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.ACCEPTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.AGREED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.FINALIZED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.TERMINATED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.VERIFIED;

/**
 * Manages contract negotiations on a provider.
 */
public class ProviderNegotiationManagerImpl extends AbstractNegotiationManager implements ProviderNegotiationManager {

    @Override
    public void offered(String providerId) {
        var negotiation = negotiations.get(providerId);
        negotiation.transition(OFFERED, n -> listeners.forEach(l -> l.offered(n)));
    }

    @Override
    public void agreed(String providerId) {
        var negotiation = negotiations.get(providerId);
        negotiation.transition(AGREED, n -> listeners.forEach(l -> l.agreed(n)));
    }

    @Override
    public void finalized(String providerId) {
        var negotiation = negotiations.get(providerId);
        negotiation.transition(FINALIZED, n -> listeners.forEach(l -> l.finalized(n)));
    }

    @Override
    public Map<String, Object> handleContractRequest(Map<String, Object> contractRequest, String counterPartyId) {
        ContractNegotiation negotiation;
        if (contractRequest.containsKey(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED)) {
            // the message is a counter-offer
            var processId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, contractRequest);
            negotiation = handleCounterOffer(contractRequest, processId);
        } else {
            // the message is an initial request
            negotiation = handleInitialRequest(contractRequest, counterPartyId);
        }
        return createNegotiationResponse(negotiation.getId(), negotiation.getCorrelationId(), negotiation.getState().toString().toLowerCase());
    }

    @Override
    public void handleAccepted(Map<String, Object> event) {
        var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, event); // // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
        stringProperty(DSPACE_PROPERTY_EVENT_TYPE_EXPANDED, event);
        var negotiation = negotiations.get(providerId);
        negotiation.transition(ACCEPTED, n -> listeners.forEach(l -> l.agreed(negotiation)));
    }

    @Override
    public void handleVerified(Map<String, Object> verification) {
        var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, verification); // FIXME https://github.com/eclipse-dataspacetck/cvf/issues/92
        var negotiation = findById(providerId);
        // TODO verify message
        negotiation.transition(VERIFIED, n -> listeners.forEach(l -> l.verified(n)));
    }

    @Override
    public void terminated(Map<String, Object> termination) {
        var processId = requireNonNull(stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, termination));
        var negotiation = negotiations.get(processId);
        negotiation.transition(TERMINATED, n -> listeners.forEach(l -> l.terminated(n)));
    }

    @NotNull
    private ContractNegotiation handleCounterOffer(Map<String, Object> contractRequest, String processId) {
        var negotiation = findById(processId);
        var offer = mapProperty(DSPACE_PROPERTY_OFFER_EXPANDED, contractRequest);
        negotiation.storeOffer(offer, REQUESTED, n -> listeners.forEach(l -> l.contractRequested(negotiation)));
        return negotiation;
    }

    @NotNull
    private ContractNegotiation handleInitialRequest(Map<String, Object> contractRequest, String counterPartyId) {
        @DcpSpecBug(section = "Json-LD context", description = "https://github.com/eclipse-dataspacetck/cvf/issues/92")
        var consumerId = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, contractRequest);
        var previousNegotiation = findByCorrelationId(consumerId);
        if (previousNegotiation != null) {
            return previousNegotiation;
        }

        var offer = mapProperty(DSPACE_PROPERTY_OFFER_EXPANDED, contractRequest);

        var offerId = compactStringProperty(ID, offer);
        var callbackAddress = stringProperty(DSPACE_PROPERTY_CALLBACK_ADDRESS_EXPANDED, contractRequest);

        var negotiation = ContractNegotiation.Builder.newInstance()
                .correlationId(consumerId)
                .offerId(offerId)
                .datasetId(datasetIdFromOfferId(offerId))
                .state(REQUESTED)
                .counterPartyId(counterPartyId)
                .callbackAddress(callbackAddress)
                .build();

        negotiations.put(negotiation.getId(), negotiation);
        listeners.forEach(l -> l.contractRequested(negotiation));

        return negotiation;
    }

}
