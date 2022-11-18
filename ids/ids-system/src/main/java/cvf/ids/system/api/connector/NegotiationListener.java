package cvf.ids.system.api.connector;

import cvf.ids.system.api.statemachine.ContractNegotiation;

/**
 * Defines base listener events for contract negotiations.
 */
@SuppressWarnings("unused")
public interface NegotiationListener {

    /**
     * Invoked when a termination has been received.
     */
    default void terminated(ContractNegotiation negotiation) {
    }

}
