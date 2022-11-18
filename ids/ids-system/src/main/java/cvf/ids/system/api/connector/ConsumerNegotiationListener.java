package cvf.ids.system.api.connector;

import cvf.ids.system.api.statemachine.ContractNegotiation;

/**
 * Implementations can be registered to receive contract negotiation events for a consumer.
 */
public interface ConsumerNegotiationListener extends NegotiationListener {

    /**
     * Invoked when a contract negotiation is created.
     */
    void negotiationCreated(ContractNegotiation negotiation);

}
