package org.eclipse.dataspacetck.dsp.system.mock;

import org.eclipse.dataspacetck.dsp.system.api.mock.NegotiationProviderMock;

/**
 * A no-op mock used when the local connector is disabled.
 */
public class NoOpNegotiationProviderMock implements NegotiationProviderMock {

    @Override
    public void recordContractRequestedAction(Action action) {

    }

    @Override
    public void recordConsumerAgreedAction(Action action) {

    }

    @Override
    public void recordConsumerVerifyAction(Action action) {

    }

    @Override
    public void verify() {

    }

    @Override
    public boolean completed() {
        return true;
    }

    @Override
    public void reset() {

    }
}
