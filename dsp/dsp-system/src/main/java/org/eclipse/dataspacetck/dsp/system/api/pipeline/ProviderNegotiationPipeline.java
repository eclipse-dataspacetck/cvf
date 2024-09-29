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

package org.eclipse.dataspacetck.dsp.system.api.pipeline;

import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A pipeline that drives message interactions with a provider connector under test. Uses a TCK consumer connector to interact with
 * the provider connector being verified.
 */
public interface ProviderNegotiationPipeline extends NegotiationPipeline<ProviderNegotiationPipeline> {

    ProviderNegotiationPipeline sendRequestMessage(String datasetId, String offerId);

    ProviderNegotiationPipeline sendCounterOfferMessage(String offerId, String targetId);

    ProviderNegotiationPipeline sendCounterOfferMessage(String offerId, String targetId, boolean expectError);

    ProviderNegotiationPipeline sendTermination();

    ProviderNegotiationPipeline sendTermination(boolean expectError);

    ProviderNegotiationPipeline acceptLastOffer();

    ProviderNegotiationPipeline sendVerifiedEvent();

    ProviderNegotiationPipeline sendVerifiedEvent(boolean expectError);

    ProviderNegotiationPipeline expectOfferMessage(Function<Map<String, Object>, Map<String, Object>> action);

    ProviderNegotiationPipeline expectAgreementMessage(Consumer<Map<String, Object>> action);

    ProviderNegotiationPipeline expectFinalizedEvent(Consumer<Map<String, Object>> action);

    ProviderNegotiationPipeline expectTermination();

    ProviderNegotiationPipeline then(Runnable runnable);

    @SuppressWarnings("unused")
    ProviderNegotiationPipeline thenVerify(Runnable runnable);

    @SuppressWarnings("unused")
    ProviderNegotiationPipeline thenVerifyNegotiation(Consumer<ContractNegotiation> consumer);

    @SuppressWarnings("unused")
    ProviderNegotiationPipeline thenVerifyState(ContractNegotiation.State state);

    ProviderNegotiationPipeline thenVerifyProviderState(ContractNegotiation.State state);


}
