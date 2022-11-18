package cvf.ids.system.api.statemachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_AGREED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_REQUESTED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.CONSUMER_VERIFIED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_AGREED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.PROVIDER_FINALIZED;
import static cvf.ids.system.api.statemachine.ContractNegotiation.State.TERMINATED;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

/**
 * The contract negotiation entity.
 */
public class ContractNegotiation {

    public enum State {
        INITIALIZED,
        CONSUMER_REQUESTED,
        PROVIDER_OFFERED,
        CONSUMER_AGREED,
        PROVIDER_AGREED,
        CONSUMER_VERIFIED,
        PROVIDER_FINALIZED,
        TERMINATED
    }

    private String id;
    private String correlationId;
    private String offerId;
    private String datasetId;
    private String callbackAddress;

    private State state = State.INITIALIZED;

    private List<BiConsumer<State, ContractNegotiation>> listeners = new ArrayList<>();

    public String getId() {
        return id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String id) {
        this.correlationId = id;
    }

    public State getState() {
        return state;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public String getCallbackAddress() {
        return callbackAddress;
    }

    public void transition(State newState) throws IllegalStateException {
        final var oldState = state;
        switch (state) {
            case INITIALIZED:
                assertStates(newState, CONSUMER_REQUESTED, State.PROVIDER_OFFERED, TERMINATED);
                verifyCorrelationId(newState);
                state = newState;
                listeners.forEach(l -> l.accept(oldState, this));
                break;
            case CONSUMER_REQUESTED:
                assertStates(newState, CONSUMER_REQUESTED, State.PROVIDER_OFFERED, PROVIDER_AGREED, TERMINATED);
                state = newState;
                listeners.forEach(l -> l.accept(oldState, this));
                break;
            case PROVIDER_OFFERED:
                assertStates(newState, CONSUMER_REQUESTED, State.PROVIDER_OFFERED, CONSUMER_AGREED, TERMINATED);
                state = newState;
                listeners.forEach(l -> l.accept(oldState, this));
                break;
            case CONSUMER_AGREED:
                assertStates(newState, PROVIDER_AGREED, TERMINATED);
                state = newState;
                listeners.forEach(l -> l.accept(oldState, this));
                break;
            case PROVIDER_AGREED:
                assertStates(newState, CONSUMER_VERIFIED, TERMINATED);
                state = newState;
                listeners.forEach(l -> l.accept(oldState, this));
                break;
            case CONSUMER_VERIFIED:
                assertStates(newState, PROVIDER_FINALIZED, TERMINATED);
                state = newState;
                listeners.forEach(l -> l.accept(oldState, this));
                break;
            case PROVIDER_FINALIZED:
                throw new IllegalStateException(PROVIDER_FINALIZED + " is a final state");
            case TERMINATED:
                throw new IllegalStateException(TERMINATED + " is a final state");
        }
    }

    private void verifyCorrelationId(State newState) {
        if (newState == CONSUMER_REQUESTED || newState == State.PROVIDER_OFFERED) {
            if (correlationId == null) {
                throw new IllegalStateException("Correlation id not set");
            }
        }
    }

    private void assertStates(State currentState, State... states) {
        for (var state : states) {
            if (currentState == state) {
                return;
            }
        }
        throw new IllegalStateException(format("Current state %s not one of %s", state, Arrays.stream(states).map(Enum::toString).collect(Collectors.joining(", "))));
    }

    private ContractNegotiation() {
    }

    public static class Builder {
        private ContractNegotiation negotiation;

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder correlationId(String correlationId) {
            negotiation.correlationId = correlationId;
            return this;
        }

        public Builder offerId(String offerId) {
            negotiation.offerId = offerId;
            return this;
        }

        public Builder datasetId(String datasetId) {
            negotiation.datasetId = datasetId;
            return this;
        }

        public Builder listener(BiConsumer<State, ContractNegotiation> listener) {
            negotiation.listeners.add(listener);
            return this;
        }

        public Builder state(State state) {
            this.negotiation.state = state;
            return this;
        }

        public Builder callbackAddress(String callbackAddress) {
            this.negotiation.callbackAddress = callbackAddress;
            return this;
        }

        public ContractNegotiation build() {
            requireNonNull(negotiation.datasetId);
            negotiation.id = randomUUID().toString();
            negotiation.verifyCorrelationId(negotiation.state);
            return negotiation;
        }

        private Builder() {
            negotiation = new ContractNegotiation();
        }

    }
}
