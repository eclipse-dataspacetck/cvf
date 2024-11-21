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

import org.eclipse.dataspacetck.api.system.MandatoryTest;
import org.eclipse.dataspacetck.api.system.TestSequenceDiagram;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static org.eclipse.dataspacetck.dsp.system.api.connector.IdGenerator.offerIdFromDatasetId;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.ACCEPTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.FINALIZED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.VERIFIED;

@Tag("base-compliance")
@DisplayName("CN_C_01: Contract request scenarios")
public class ContractNegotiationConsumer01Test extends AbstractContractNegotiationConsumerTest {

    @MandatoryTest
    @DisplayName("CN_C:01-01: Verify contract request, offer received, consumer accepted, provider agreed, consumer verified, provider finalized")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start negotiation
            
            CUT->>TCK: ContractRequestMessage
            TCK-->>CUT: ContractNegotiation
            
            TCK->>CUT: ContractOfferMessage
            CUT-->>TCK: 200 OK
            
            CUT->>TCK: ContractNegotiationEventMessage:accepted
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: ContractAgreementMessage
            CUT-->>TCK: 200 OK
            
            CUT->>TCK: ContractAgreementVerificationMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: ContractNegotiationEventMessage:finalized
            CUT-->>TCK: 200 OK
            """)
    public void cn_c_01_01() {
        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);
        negotiationMock.recordOfferedAction(ConsumerActions::postAccepted);
        negotiationMock.recordAgreedAction(ConsumerActions::postVerification);

        negotiationPipeline
                .expectRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest("C0101", offerIdFromDatasetId("C0101"))
                .thenWaitForState(REQUESTED)
                .expectAcceptedEvent(event -> providerConnector.getProviderNegotiationManager().handleAccepted(event))
                .sendOfferMessage()
                .thenWaitForState(ACCEPTED)
                .expectVerifiedMessage(verified -> providerConnector.getProviderNegotiationManager().handleVerified(verified))
                .sendAgreementMessage()
                .thenWaitForState(VERIFIED)
                .sendFinalizedEvent()
                .thenVerifyConsumerState(FINALIZED)
                .execute();

        negotiationMock.verify();
    }

}
