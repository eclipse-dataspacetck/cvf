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

import org.eclipse.dataspacetck.core.api.system.MandatoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.AGREED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.FINALIZED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;

@Tag("base-compliance")
@DisplayName("CN_03: Provider negative test scenarios")
public class ContractNegotiationProvider03Test extends AbstractContractNegotiationProviderTest {

    @MandatoryTest
    @DisplayName("CN:03-01: Verify contract request, provider agreement, consumer verified, provider finalized, invalid consumer terminated")
    public void cn_03_01() {
        // Sends an invalid terminate message that should result in an error
        negotiationMock.recordContractRequestedAction(ProviderActions::postProviderAgreed);
        negotiationMock.recordConsumerVerifyAction(ProviderActions::postProviderFinalized);

        negotiationPipeline
                .expectAgreement(agreement -> consumerConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(AGREED)
                .expectFinalized(event -> consumerConnector.getConsumerNegotiationManager().handleFinalized(event))
                .sendConsumerVerify()
                .thenWaitForState(FINALIZED)
                .sendTermination(true)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:03-02: Verify contract request, offer received, invalid consumer verified")
    public void cn_03_02() {
        // Sends an invalid consumer verified that should result in an error
        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);

        negotiationPipeline
                .expectOffer(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .sendConsumerVerify(true)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:03-03: Verify contract request, offer received, consumer accepted, illegal consumer verified")
    public void cn_03_03() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);

        negotiationPipeline
                .expectOffer(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .acceptLastOffer()
                .sendConsumerVerify(true)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:03-04: Verify contract request, offer received, consumer counter-offer (x2), provider terminated")
    public void cn_03_04() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordContractRequestedAction(cn -> {
        });

        negotiationPipeline
                .expectOffer(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .sendCounterRequest("CD123:ACN0304:456", "ACN0304")
                .sendCounterRequest("CD123:ACN0304:456", "ACN0304", true) // send second offer
                .execute();

        negotiationMock.verify();
    }


}
