package cvf.ids.system.api.mock;

import cvf.ids.system.api.statemachine.ContractNegotiation;

import java.util.function.Consumer;

/**
 * Mock service for recording provider connector actions.
 */
public interface NegotiationProviderMock {

    /**
     * An action to be executed by the mock.
     */
    interface Action extends Consumer<ContractNegotiation> {
    }

    /**
     * Records an action to execute when a contract request is processed on the provider.
     */
    void recordContractRequestedAction(Action action);

    /**
     * Records an action to execute when a consumer agrees to an offer.
     */
    void recordConsumerAgreedAction(Action action);

    /**
     * Records an action to execute when a consumer verifies an offer.
     */
    void recordConsumerVerifyAction(Action action);

    /**
     * Verifies all actions have been executed.
     */
    void verify();

    /**
     * Returns true if all actions have been executed.
     */
    boolean completed();

    /**
     * Resets the mock and all recorded actions.
     */
    void reset();

}
