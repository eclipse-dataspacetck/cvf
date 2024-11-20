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

import org.eclipse.dataspacetck.api.system.TestSequenceDiagram;
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
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)

            TCK->>CUT: ContractRequestMessage
            CUT-->>TCK: ContractNegotiation
            
            CUT->>TCK: ContractAgreementMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: ContractVerificationMessage
            CUT-->>TCK: 200 OK
            
            CUT->>TCK: ContractNegotiationEventMessage:finalized
            TCk-->>CUT: 200 OK
            
            TCK->>CUT: ContractNegotiationTerminationMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void cn_03_01() {

        // Sends an invalid terminate message that should result in an error
        negotiationMock.recordContractRequestedAction(ProviderActions::postAgreed);
        negotiationMock.recordVerifiedAction(ProviderActions::postFinalized);

        negotiationPipeline
                .expectAgreementMessage(agreement -> consumerConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .sendRequestMessage(datasetId, offerId)
                .thenWaitForState(AGREED)
                .expectFinalizedEvent(event -> consumerConnector.getConsumerNegotiationManager().handleFinalized(event))
                .sendVerifiedEvent()
                .thenWaitForState(FINALIZED)
                .sendTermination(true)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:03-02: Verify contract request, offer received, invalid consumer verified")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)

            TCK->>CUT: ContractRequestMessage
            CUT-->>TCK: ContractNegotiation
            
            CUT->>TCK: ContractOfferMessage
            TCK-->>CUT: 200 OK
            
            TCk->>CUT: ContractVerificationMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void cn_03_02() {

        // Sends an invalid consumer verified that should result in an error
        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);

        negotiationPipeline
                .expectOfferMessage(offer -> consumerConnector.getConsumerNegotiationManager().handleOffer(offer))
                .sendRequestMessage(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .sendVerifiedEvent(true)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:03-03: Verify contract request, offer received, consumer accepted, illegal consumer verified")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)

            TCK->>CUT: ContractRequestMessage
            CUT-->>TCK: ContractNegotiation
            
            CUT->>TCK: ContractOfferMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: ContractNegotiationEventMessage:accepted
            CUT-->>TCK: 200 OK
            
            TCk->>CUT: ContractVerificationMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void cn_03_03() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);

        negotiationPipeline
                .expectOfferMessage(offer -> consumerConnector.getConsumerNegotiationManager().handleOffer(offer))
                .sendRequestMessage(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .acceptLastOffer()
                .sendVerifiedEvent(true)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:03-04: Verify contract request, offer received, consumer counter-offer (x2), provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)

            TCK->>CUT: ContractRequestMessage
            CUT-->>TCK: ContractNegotiation
            
            CUT->>TCK: ContractOfferMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: ContractRequestMessage
            CUT-->>TCK: ContractNegotiation
            
            TCK->>CUT: ContractRequestMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void cn_03_04() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordContractRequestedAction(cn -> {
        });

        negotiationPipeline
                .expectOfferMessage(offer -> consumerConnector.getConsumerNegotiationManager().handleOffer(offer))
                .sendRequestMessage(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .sendCounterOfferMessage("CD123:ACN0304:456", "ACN0304")
                .sendCounterOfferMessage("CD123:ACN0304:456", "ACN0304", true) // send second offer
                .execute();

        negotiationMock.verify();
    }


}
