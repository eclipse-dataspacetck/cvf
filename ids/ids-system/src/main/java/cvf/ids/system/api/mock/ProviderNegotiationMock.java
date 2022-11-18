package cvf.ids.system.api.mock;

import cvf.ids.system.api.statemachine.ContractNegotiation;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Mock service for recording provider connector actions.
 */
public interface ProviderNegotiationMock {

    /**
     * Records an action to execute when a contract request is processed on the provider.
     */
    void recordContractRequestedAction(BiConsumer<Map<String, Object>, ContractNegotiation> action);

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
