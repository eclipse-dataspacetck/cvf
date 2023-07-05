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

package cvf.ids;

import cvf.core.api.system.ConfigParam;
import cvf.core.api.system.Inject;
import cvf.core.api.system.MandatoryTest;
import cvf.ids.system.api.connector.Connector;
import cvf.ids.system.api.connector.Consumer;
import cvf.ids.system.api.mock.NegotiationProviderMock;
import cvf.ids.system.api.pipeline.NegotiationPipeline;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_AGREED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_FINALIZED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_OFFERED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.TERMINATED;
import static java.util.UUID.randomUUID;

@Tag("base-compliance")
@DisplayName("CN_01: Contract request scenarios")
public class IdsContractNegotiationMandatory01Test extends AbstractNegotiationVerificationTest {

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
                .sendRequest(datasetId, offerId)
                .thenWaitForState(PROVIDER_OFFERED)
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
                .sendRequest(datasetId, offerId)
                .thenWaitForState(PROVIDER_OFFERED)
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
                .sendRequest(datasetId, offerId)
                .thenWaitForState(PROVIDER_OFFERED)
                .expectAgreement(agreement -> clientConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .acceptLastOffer()
                .thenWaitForState(PROVIDER_AGREED)
                .expectFinalized(event -> clientConnector.getConsumerNegotiationManager().handleFinalized(event))
                .sendConsumerVerify()
                .thenWaitForState(PROVIDER_FINALIZED)
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
                .sendRequest(datasetId, offerId)
                .thenWaitForState(PROVIDER_AGREED)
                .expectFinalized(event -> clientConnector.getConsumerNegotiationManager().handleFinalized(event))
                .sendConsumerVerify()
                .thenWaitForState(PROVIDER_FINALIZED)
                .execute();

        negotiationMock.verify();
    }


}
