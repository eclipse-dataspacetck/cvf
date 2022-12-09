package cvf.ids;

import cvf.core.api.system.Inject;
import cvf.ids.system.api.connector.Client;
import cvf.ids.system.api.connector.Connector;
import cvf.ids.system.api.mock.ProviderNegotiationMock;
import cvf.ids.system.api.pipeline.NegotiationPipeline;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_AGREED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_OFFERED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.TERMINATED;
import static java.util.UUID.randomUUID;

/**
 *
 */
@Tag("base-compliance")
@DisplayName("IDS-01: Contract request scenarios")
public class IdsVerification1Test extends AbstractNegotiationVerificationTest {

    @Inject
    @Client
    private Connector clientConnector;

    @Inject
    private NegotiationPipeline negotiationPipeline;

    @Inject
    protected ProviderNegotiationMock negotiationMock;

    @Test
    @DisplayName("IDS-01-01: Verify contract request, offer received, and consumer terminated")
    public void verify_01_01() {
        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);

        var datasetId = randomUUID().toString();
        var offerId = randomUUID().toString();

        negotiationPipeline
                .expectOffer(offer -> clientConnector.getConsumerNegotiationManager().providerOffer(offer))
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

        var datasetId = randomUUID().toString();
        var offerId = randomUUID().toString();

        negotiationPipeline
                .expectOffer(offer -> clientConnector.getConsumerNegotiationManager().providerOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(PROVIDER_OFFERED)
                .expectTermination()
                .sendCounterRequest()
                .thenWaitForState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }


    @Test
    @DisplayName("IDS-01-02: Verify contract request, offer received, consumer accepted, provider agreement, consumer verified")
    public void verify_01_03() {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordConsumerAgreedAction(ProviderActions::postProviderAgreed);

        var datasetId = randomUUID().toString();
        var offerId = randomUUID().toString();

        negotiationPipeline
                .expectOffer(offer -> clientConnector.getConsumerNegotiationManager().providerOffer(offer))
                .sendRequest(datasetId, offerId)
                .thenWaitForState(PROVIDER_OFFERED)
                .expectAgreement(agreement -> clientConnector.getConsumerNegotiationManager().handleAgreement(agreement))
                .acceptLastOffer()
                .thenWaitForState(PROVIDER_AGREED)
                .sendConsumerVerify()
                .execute();

        negotiationMock.verify();
    }

}
