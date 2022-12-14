package cvf.ids.system.mock;

import cvf.ids.system.api.connector.ProviderNegotiationListener;
import cvf.ids.system.api.connector.ProviderNegotiationManager;
import cvf.ids.system.api.mock.NegotiationProviderMock;
import cvf.ids.system.api.statemachine.ContractNegotiation;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_AGREED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_REQUESTED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_VERIFIED;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

/**
 * Default mock implementation.
 */
public class NegotiationProviderMockImpl implements NegotiationProviderMock, ProviderNegotiationListener {
    private ProviderNegotiationManager manager;
    private Executor executor;
    private static final Queue<Action> EMPTY_QUEUE = new ArrayDeque<>();

    private Map<ContractNegotiation.State, Queue<Action>> actions = new ConcurrentHashMap<>();

    public NegotiationProviderMockImpl(ProviderNegotiationManager manager, Executor executor) {
        this.manager = manager;
        this.executor = executor;
        manager.registerListener(this);
    }

    @Override
    public void recordContractRequestedAction(Action action) {
        recordAction(CONSUMER_REQUESTED, action);
    }

    @Override
    public void recordConsumerAgreedAction(Action action) {
        recordAction(CONSUMER_AGREED, action);
    }

    @Override
    public void recordConsumerVerifyAction(Action action) {
        recordAction(CONSUMER_VERIFIED, action);
    }

    @Override
    public void contractRequested(Map<String, Object> contractRequest, ContractNegotiation negotiation) {
        var action = actions.getOrDefault(CONSUMER_REQUESTED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(negotiation));
    }

    @Override
    public void consumerAgreed(ContractNegotiation negotiation) {
        var action = actions.getOrDefault(CONSUMER_AGREED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(negotiation));
    }

    @Override
    public void consumerVerified(Map<String, Object> verification, ContractNegotiation negotiation) {
        var action = actions.getOrDefault(CONSUMER_VERIFIED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(negotiation));
    }

    public void verify() {
        if (!actions.isEmpty()) {
            var actions = this.actions.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .map(e -> e.getKey().toString())
                    .collect(toList());
            if (!actions.isEmpty()) {
                throw new AssertionError(format("Request actions not executed.\n Actions: %s", join(", ", actions)));
            }
        }
        manager.deregisterListener(this);
    }

    @Override
    public boolean completed() {
        return actions.isEmpty();
    }

    @Override
    public void reset() {
        actions.clear();
    }

    private boolean recordAction(ContractNegotiation.State state, Action action) {
        return actions.computeIfAbsent(state, k -> new ConcurrentLinkedQueue<>()).add(action);
    }

}
