/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 *
 */

package cvf.ids.system.api.statemachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
 * <p>
 * This implementation is thread-safe.
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

    private static final Consumer<ContractNegotiation> NULL_WORK = n -> {
    };

    private String id;
    private String correlationId;
    private String offerId;
    private String datasetId;
    private String callbackAddress;

    private State state = State.INITIALIZED;

    private List<BiConsumer<State, ContractNegotiation>> listeners = new ArrayList<>();

    private List<Map<String, Object>> offers = new ArrayList<>();

    private Map<String, Object> agreement;

    private LockManager lockManager = new LockManager();

    public String getId() {
        return id;
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

    public String getCorrelationId() {
        return lockManager.readLock(() -> correlationId);
    }

    public State getState() {
        return lockManager.readLock(() -> state);
    }

    public Map<String, Object> getLastOffer() {
        return lockManager.readLock(() -> offers.isEmpty() ? null : offers.get(offers.size() - 1));
    }

    public List<Map<String, Object>> getOffers() {
        return lockManager.readLock(() -> new ArrayList<>(offers));
    }

    public Map<String, Object> getAgreement() {
        return lockManager.readLock(() -> agreement);
    }

    /**
     * Sets the correlation id.
     */
    public void setCorrelationId(String id, State state) {
        lockManager.writeLock(() -> {
            this.correlationId = id;
            transition(state);
            return null;
        });
    }

    /**
     * Stores the offer.
     */
    public void storeOffer(Map<String, Object> offer, State state) {
        storeOffer(offer, state, NULL_WORK);
    }

    /**
     * Stores the offer and executes the work while holding a write lock.
     */
    public void storeOffer(Map<String, Object> offer, State state, Consumer<ContractNegotiation> work) {
        lockManager.writeLock(() -> {
            transition(state);
            offers.add(offer);
            return null;
        });
        work.accept(this);
    }

    /**
     * Stores the agreement.
     */
    public void storeAgreement(Map<String, Object> agreement) {
        storeAgreement(agreement, NULL_WORK);
    }

    /**
     * Stores the agreement and executes the work while holding a write lock.
     */
    public void storeAgreement(Map<String, Object> agreement, Consumer<ContractNegotiation> work) {
        lockManager.writeLock(() -> {
            transition(PROVIDER_AGREED);
            this.agreement = agreement;
            return null;
        });
        work.accept(this);
    }

    /**
     * Transitions to the new state.
     */
    public void transition(State newState) throws IllegalStateException {
        transition(newState, NULL_WORK);
    }

    /**
     * Transitions to the new state and executes the work while holding a write-lock.
     */
    public void transition(State newState, Consumer<ContractNegotiation> work) throws IllegalStateException {
        lockManager.writeLock(() -> {
            var oldState = state;
            switch (state) {
                case INITIALIZED -> {
                    assertStates(newState, CONSUMER_REQUESTED, State.PROVIDER_OFFERED, TERMINATED);
                    verifyCorrelationId(newState);
                    state = newState;
                    listeners.forEach(l -> l.accept(oldState, this));
                }
                case CONSUMER_REQUESTED -> {
                    assertStates(newState, CONSUMER_REQUESTED, State.PROVIDER_OFFERED, PROVIDER_AGREED, TERMINATED);
                    state = newState;
                    listeners.forEach(l -> l.accept(oldState, this));
                }
                case PROVIDER_OFFERED -> {
                    assertStates(newState, CONSUMER_REQUESTED, State.PROVIDER_OFFERED, CONSUMER_AGREED, TERMINATED);
                    state = newState;
                    listeners.forEach(l -> l.accept(oldState, this));
                }
                case CONSUMER_AGREED -> {
                    assertStates(newState, PROVIDER_AGREED, TERMINATED);
                    state = newState;
                    listeners.forEach(l -> l.accept(oldState, this));
                }
                case PROVIDER_AGREED -> {
                    assertStates(newState, CONSUMER_VERIFIED, TERMINATED);
                    state = newState;
                    listeners.forEach(l -> l.accept(oldState, this));
                }
                case CONSUMER_VERIFIED -> {
                    assertStates(newState, PROVIDER_FINALIZED, TERMINATED);
                    state = newState;
                    listeners.forEach(l -> l.accept(oldState, this));
                }
                case PROVIDER_FINALIZED -> throw new IllegalStateException(PROVIDER_FINALIZED + " is a final state");
                case TERMINATED -> throw new IllegalStateException(TERMINATED + " is a final state");
                default -> throw new IllegalStateException("Unexpected value: " + state);
            }
            work.accept(this);
            return null;
        });
    }

    private void verifyCorrelationId(State newState) {
        if (newState == CONSUMER_REQUESTED || newState == State.PROVIDER_OFFERED) {
            if (correlationId == null) {
                throw new IllegalStateException("Correlation id not set");
            }
        }
    }

    private void assertStates(State toState, State... states) {
        for (var state : states) {
            if (toState == state) {
                return;
            }
        }
        var legalStates = Arrays.stream(states).map(Enum::toString).collect(Collectors.joining(", "));
        throw new IllegalStateException(format("Illegal state transition from %s to %s. To state must be one of %s.", this.state, toState, legalStates));
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
