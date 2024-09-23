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
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.TERMINATED;
import static org.eclipse.dataspacetck.dsp.verification.cn.ProviderActions.pause;
import static org.eclipse.dataspacetck.dsp.verification.cn.ProviderActions.postOffer;
import static org.eclipse.dataspacetck.dsp.verification.cn.ProviderActions.terminate;

@Tag("base-compliance")
@DisplayName("CN_02: Provider test scenarios")
public class ContractNegotiationProvider02Test extends AbstractContractNegotiationProviderTest {

    @MandatoryTest
    @DisplayName("CN:02-01: Verify contract request, provider terminated")
    public void cn_02_01() {

        negotiationMock.recordContractRequestedAction(ProviderActions::terminate);

        negotiationPipeline
                .sendRequest(datasetId, offerId)
                .expectTermination()
                .thenWaitForState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:02-02: Verify contract request, consumer terminated")
    public void cn_02_02() {

        negotiationPipeline
                .sendRequest(datasetId, offerId)
                .sendTermination()
                .thenVerifyProviderState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:02-03: Verify contract request, provider agreement, consumer terminated")
    public void cn_02_03() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postProviderAgreed);

        negotiationPipeline
                .expectAgreement(agreement -> consumerConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(AGREED)
                .sendTermination()
                .thenVerifyProviderState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:02-04: Verify contract request, offer received, consumer terminated")
    public void cn_02_04() {

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
    @DisplayName("CN:02-05: Verify contract request, offer received, provider terminated")
    public void cn_02_05() {

        negotiationMock.recordContractRequestedAction(negotiation -> {
            postOffer(negotiation);
            pause();
            terminate(negotiation);
        });

        negotiationPipeline
                .expectOffer(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .expectTermination()
                .thenWaitForState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:02-06: Verify contract request, offer received, consumer accepted, provider terminated")
    public void cn_02_06() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordConsumerAgreedAction(ProviderActions::terminate);

        negotiationPipeline
                .expectOffer(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(OFFERED)
                .acceptLastOffer()
                .expectTermination()
                .thenWaitForState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:02-07: Verify contract request, provider agreement, consumer verified, provider terminated")
    public void cn_02_07() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postProviderAgreed);
        negotiationMock.recordConsumerVerifyAction(ProviderActions::terminate);

        negotiationPipeline
                .expectAgreement(agreement -> consumerConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(AGREED)
                .expectTermination()
                .sendConsumerVerify()
                .thenWaitForState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }


}
