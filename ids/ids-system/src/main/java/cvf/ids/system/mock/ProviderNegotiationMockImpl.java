package cvf.ids.system.mock;

import cvf.ids.system.api.connector.ProviderNegotiationManager;
import cvf.ids.system.api.connector.ProviderNegotiationListener;
import cvf.ids.system.api.mock.ProviderNegotiationMock;
import cvf.ids.system.api.statemachine.ContractNegotiation;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

/**
 * Default mock implementation.
 */
public class ProviderNegotiationMockImpl implements ProviderNegotiationMock, ProviderNegotiationListener {
    private ProviderNegotiationManager manager;
    private Queue<BiConsumer<Map<String, Object>, ContractNegotiation>> requestedActions = new ConcurrentLinkedQueue<>();
    private Executor executor;

    public ProviderNegotiationMockImpl(ProviderNegotiationManager manager, Executor executor) {
        this.manager = manager;
        this.executor = executor;
        manager.registerListener(this);
    }

    @Override
    public void recordContractRequestedAction(BiConsumer<Map<String, Object>, ContractNegotiation> action) {
        requestedActions.add(action);
    }

    @Override
    public void contractRequested(Map<String, Object> contractRequest, ContractNegotiation negotiation) {
        var action = requestedActions.poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(contractRequest, negotiation));
    }

    public void verify() {
        if (!requestedActions.isEmpty()) {
            throw new AssertionError("Request actions not executed: " + requestedActions.size());
        }
        manager.deregisterListener(this);
    }

    @Override
    public boolean completed() {
        return requestedActions.isEmpty();
    }

    @Override
    public void reset() {
        requestedActions.clear();
    }
}
