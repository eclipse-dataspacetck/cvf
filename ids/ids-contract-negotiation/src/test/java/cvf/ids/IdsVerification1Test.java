package cvf.ids;

import cvf.core.api.system.Inject;
import cvf.ids.system.api.connector.Connector;
import cvf.ids.system.api.connector.Consumer;
import cvf.ids.system.api.mock.ProviderNegotiationMock;
import cvf.ids.system.api.pipeline.NegotiationPipeline;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static cvf.ids.NegotiationSetup.getDatasetId;
import static cvf.ids.NegotiationSetup.getOfferId;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_AGREED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_FINALIZED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_OFFERED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.TERMINATED;

/**
 *
 */
@Tag("base-compliance")
@DisplayName("IDS-01: Contract request scenarios")
public class IdsVerification1Test extends AbstractNegotiationVerificationTest {

    @Inject
    @Consumer
    private Connector clientConnector;

    @Inject
    private NegotiationPipeline negotiationPipeline;

    @Inject
    protected ProviderNegotiationMock negotiationMock;

    @Test
    @DisplayName("IDS-01-01: Verify contract request, offer received, consumer terminated")
    public void verify_01_01() {
        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);

        var datasetId = getDatasetId("CN_DS_01_01");
        var offerId = getOfferId("CN_O_01_01");

        negotiationPipeline
                .expectOffer(offer -> clientConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(PROVIDER_OFFERED)
                .sendTermination()
                .thenVerifyProviderState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @Test
    @DisplayName("IDS-01-02: Verify contract request, offer received, consumer counter-offer, provider terminated")
    public void verify_01_02() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordContractRequestedAction(ProviderActions::terminate);

        var datasetId = getDatasetId("CN_DS_01_02");
        var offerId = getOfferId("CN_O_01_02");

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

    @Test
    @DisplayName("IDS-01-03: Verify contract request, offer received, consumer accepted, provider agreement, consumer verified, provider finalized")
    public void verify_01_03() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordConsumerAgreedAction(ProviderActions::postProviderAgreed);
        negotiationMock.recordConsumerVerifyAction(ProviderActions::postProviderFinalized);

        var datasetId = getDatasetId("CN_DS_01_03");
        var offerId = getOfferId("CN_O_01_03");

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

}
