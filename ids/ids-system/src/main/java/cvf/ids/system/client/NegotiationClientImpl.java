package cvf.ids.system.client;

import cvf.ids.system.api.client.NegotiationClient;
import cvf.ids.system.api.connector.Connector;

import java.util.Map;

import static cvf.ids.system.api.messages.MessageFunctions.createNegotiationResponse;
import static java.util.Collections.emptyMap;

/**
 * Default implementation that suports dispatch to a local, in-memory test connector or a remote connector system via HTTP.
 */
public class NegotiationClientImpl implements NegotiationClient {
    private Connector systemConnector;

    public NegotiationClientImpl() {
    }

    public NegotiationClientImpl(Connector systemConnector) {
        this.systemConnector = systemConnector;
    }

    @Override
    public Map<String, Object> contractRequest(Map<String, Object> contractRequest) {
        if (systemConnector != null) {
            var negotiation = systemConnector.getProviderNegotiationManager().contractRequest(contractRequest);
            return createNegotiationResponse(negotiation.getId(), negotiation.getState().toString().toLowerCase());
        }
        // TODO implement HTTP invoke
        return emptyMap();
    }

    @Override
    public void terminate(Map<String, Object> termination) {
        if (systemConnector != null) {
            systemConnector.getProviderNegotiationManager().terminate(termination);
        } else {
            // TODO implement HTTP invoke
        }
    }

    @Override
    public Map<String, Object> getNegotiation(String id) {
        if (systemConnector != null) {
            var negotiation = systemConnector.getProviderNegotiationManager().findById(id);
            return createNegotiationResponse(negotiation.getId(), negotiation.getState().toString().toLowerCase());
        }
        // TODO implement HTTP invoke
        return emptyMap();

    }
}
