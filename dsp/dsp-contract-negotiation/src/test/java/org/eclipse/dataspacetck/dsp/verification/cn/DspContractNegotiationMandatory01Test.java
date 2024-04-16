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

import org.eclipse.dataspacetck.core.api.system.ConfigParam;
import org.eclipse.dataspacetck.core.api.system.Inject;
import org.eclipse.dataspacetck.core.api.system.MandatoryTest;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.Consumer;
import org.eclipse.dataspacetck.dsp.system.api.mock.NegotiationProviderMock;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.NegotiationPipeline;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static java.util.UUID.randomUUID;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.AGREED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.FINALIZED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.TERMINATED;

@Tag("base-compliance")
@DisplayName("CN_01: Contract request scenarios")
public class DspContractNegotiationMandatory01Test extends AbstractNegotiationVerificationTest {

    @Inject
    @Consumer
    private Connector clientConnector;

    @Inject
    private NegotiationPipeline negotiationPipeline;

    @Inject
    protected NegotiationProviderMock negotiationMock;

    @ConfigParam
    protected String offerId = randomUUID().toString();

    @ConfigParam
    protected String datasetId = randomUUID().toString();

    @MandatoryTest
    @DisplayName("Verify contract request, offer received, consumer terminated")
    public void cn_01_01() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);

        negotiationPipeline
                .expectOffer(offer -> clientConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId, datasetId)
                .thenWaitForState(OFFERED)
                .sendTermination()
                .thenVerifyProviderState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("Verify contract request, offer received, consumer counter-offer, provider terminated")
    public void cn_01_02() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordContractRequestedAction(ProviderActions::terminate);

        negotiationPipeline
                .expectOffer(offer -> clientConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId, datasetId)
                .thenWaitForState(OFFERED)
                .expectTermination()
                .sendCounterRequest()
                .thenWaitForState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("Verify contract request, offer received, consumer accepted, provider agreement, consumer verified, provider finalized")
    public void cn_01_03() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordConsumerAgreedAction(ProviderActions::postProviderAgreed);
        negotiationMock.recordConsumerVerifyAction(ProviderActions::postProviderFinalized);

        negotiationPipeline
                .expectOffer(offer -> clientConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId, datasetId)
                .thenWaitForState(OFFERED)
                .expectAgreement(agreement -> clientConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .acceptLastOffer()
                .thenWaitForState(AGREED)
                .expectFinalized(event -> clientConnector.getConsumerNegotiationManager().handleFinalized(event))
                .sendConsumerVerify()
                .thenWaitForState(FINALIZED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("Verify contract request, provider agreement, consumer verified, provider finalized")
    public void cn_01_04() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postProviderAgreed);
        negotiationMock.recordConsumerVerifyAction(ProviderActions::postProviderFinalized);

        negotiationPipeline
                .expectAgreement(agreement -> clientConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .sendRequest(datasetId, offerId, datasetId)
                .thenWaitForState(AGREED)
                .expectFinalized(event -> clientConnector.getConsumerNegotiationManager().handleFinalized(event))
                .sendConsumerVerify()
                .thenWaitForState(FINALIZED)
                .execute();

        negotiationMock.verify();
    }


}
