package cvf.ids;

import cvf.core.api.system.CallbackEndpoint;
import cvf.ids.system.api.client.NegotiationClient;
import cvf.ids.system.api.mock.ProviderNegotiationMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static cvf.ids.ConsumerNegotiationHandlers.handleProviderOffer;
import static cvf.ids.ProviderActions.terminate;
import static cvf.ids.system.api.pipeline.NegotiationPipeline.negotiationPipeline;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_OFFERED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.TERMINATED;

/**
 *
 */
@Tag("base-compliance")
@DisplayName("IDS-01: Contract request scenarios")
public class IdsVerification1Test extends AbstractNegotiationVerificationTest {

    @Test
    @DisplayName("IDS-01-01: Verify contract request, offer received, and consumer terminated")
    public void verify_01_01(NegotiationClient negotiationClient, CallbackEndpoint endpoint, ProviderNegotiationMock negotiationMock) {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);

        negotiationPipeline(negotiationClient, endpoint, connector)
                .expectOffer(offer -> handleProviderOffer(offer, connector))
                .sendRequest()
                .thenWaitForState(PROVIDER_OFFERED, 15)
                .sendTermination()
                .thenVerifyProviderState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @Test
    @DisplayName("IDS-01-02: Verify contract request, offer received, consumer counter-offer, provider terminated")
    public void verify_01_02(NegotiationClient negotiationClient, CallbackEndpoint endpoint, ProviderNegotiationMock negotiationMock) {

        negotiationMock.recordContractRequestedAction(ProviderActions::postOffer);
        negotiationMock.recordContractRequestedAction((request, negotiation) -> terminate(negotiation));

        negotiationPipeline(negotiationClient, endpoint, connector)
                .expectOffer(offer -> handleProviderOffer(offer, connector))
                .sendRequest()
                .thenWaitForState(PROVIDER_OFFERED, 15)
                .expectTermination()
                .sendCounterRequest()
                .thenWaitForState(TERMINATED, 15)
                .execute();

        negotiationMock.verify();
    }


}
