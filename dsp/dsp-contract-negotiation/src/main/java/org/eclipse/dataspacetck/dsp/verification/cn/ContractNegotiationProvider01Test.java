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
@DisplayName("CN_01: Contract request scenarios")
public class ContractNegotiationProvider01Test extends AbstractContractNegotiationProviderTest {

    @MandatoryTest
    @DisplayName("CN:01-01: Verify contract request, offer received, consumer terminated")
    public void cn_01_01() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);

        negotiationPipeline
                .expectOfferMessage(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequestMessage(datasetId, offerId)
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
        negotiationMock.recordContractRequestedAction(ProviderActions::postTerminate);

        negotiationPipeline
                .expectOfferMessage(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequestMessage(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .expectTermination()
                .sendCounterOfferMessage("CD123:ACN0102:456", "ACN0102")
                .thenWaitForState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:01-03: Verify contract request, offer received, consumer accepted, provider agreement, consumer verified, provider finalized")
    public void cn_01_03() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordAgreedAction(ProviderActions::postAgreed);
        negotiationMock.recordVerifiedAction(ProviderActions::postFinalized);

        negotiationPipeline
                .expectOfferMessage(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequestMessage(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .expectAgreementMessage(agreement -> consumerConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .acceptLastOffer()
                .thenWaitForState(AGREED)
                .expectFinalizedEvent(event -> consumerConnector.getConsumerNegotiationManager().handleFinalized(event))
                .sendVerifiedEvent()
                .thenWaitForState(FINALIZED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:01-04: Verify contract request, provider agreement, consumer verified, provider finalized")
    public void cn_01_04() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postAgreed);
        negotiationMock.recordVerifiedAction(ProviderActions::postFinalized);

        negotiationPipeline
                .expectAgreementMessage(agreement -> consumerConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .sendRequestMessage(datasetId, offerId)
                .thenWaitForState(AGREED)
                .expectFinalizedEvent(event -> consumerConnector.getConsumerNegotiationManager().handleFinalized(event))
                .sendVerifiedEvent()
                .thenWaitForState(FINALIZED)
                .execute();

        negotiationMock.verify();
    }


}
