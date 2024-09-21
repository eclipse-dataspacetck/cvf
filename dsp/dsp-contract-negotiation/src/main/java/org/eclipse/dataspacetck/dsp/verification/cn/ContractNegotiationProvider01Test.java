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

import org.eclipse.dataspacetck.core.api.system.MandatoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.AGREED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.FINALIZED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.TERMINATED;

@Tag("base-compliance")
@DisplayName("CN_01: Provider test scenarios")
public class ContractNegotiationProvider01Test extends AbstractContractNegotiationProviderTest {

    @MandatoryTest
    @DisplayName("CN:01-01: Verify contract request, offer received, consumer terminated")
    public void cn_01_01() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);

        negotiationPipeline
                .expectOffer(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .sendTermination()
                .thenVerifyProviderState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:01-02: Verify contract request, offer received, consumer counter-offer, provider terminated")
    public void cn_01_02() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordContractRequestedAction(ProviderActions::terminate);

        negotiationPipeline
                .expectOffer(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .expectTermination()
                .sendCounterRequest("CD123:ACN0102:456", "ACN0102")
                .thenWaitForState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:01-03: Verify contract request, offer received, consumer accepted, provider agreement, consumer verified, provider finalized")
    public void cn_01_03() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordConsumerAgreedAction(ProviderActions::postProviderAgreed);
        negotiationMock.recordConsumerVerifyAction(ProviderActions::postProviderFinalized);

        negotiationPipeline
                .expectOffer(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .expectAgreement(agreement -> consumerConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .acceptLastOffer()
                .thenWaitForState(AGREED)
                .expectFinalized(event -> consumerConnector.getConsumerNegotiationManager().handleFinalized(event))
                .sendConsumerVerify()
                .thenWaitForState(FINALIZED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:01-04: Verify contract request, provider agreement, consumer verified, provider finalized")
    public void cn_01_04() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postProviderAgreed);
        negotiationMock.recordConsumerVerifyAction(ProviderActions::postProviderFinalized);

        negotiationPipeline
                .expectAgreement(agreement -> consumerConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(AGREED)
                .expectFinalized(event -> consumerConnector.getConsumerNegotiationManager().handleFinalized(event))
                .sendConsumerVerify()
                .thenWaitForState(FINALIZED)
                .execute();

        negotiationMock.verify();
    }


}
