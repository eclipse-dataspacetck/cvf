package cvf.ids.system.api.client;

import java.util.Map;

/**
 * Proxy to the connector being verified for contract negotiation.
 */
public interface NegotiationClient {

    /**
     * Creates a contract request.
     */
    Map<String, Object> contractRequest(Map<String, Object> message);

    /**
     * Terminates a negotiation.
     */
    void terminate(Map<String, Object> termination);

    /**
     * Returns a negotiation.
     */
    Map<String, Object>  getNegotiation(String id);
}
