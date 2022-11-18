package cvf.ids;

import cvf.ids.system.api.connector.Connector;

import java.util.Map;

import static cvf.ids.system.api.message.MessageFunctions.stringProperty;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_OFFERED;

/**
 *
 */
public class ConsumerNegotiationHandlers {

    public static Void handleProviderOffer(Map<String, Object> offer, Connector connector) {
        var id = stringProperty("ids:negotiationId", offer);    // FIXME use JSON-LD to resolve namespace and key
        var negotiation = connector.getConsumerNegotiationManager().findByCorrelationId(id);
        negotiation.transition(PROVIDER_OFFERED);
        return null;
    }

    private ConsumerNegotiationHandlers() {
    }
}
