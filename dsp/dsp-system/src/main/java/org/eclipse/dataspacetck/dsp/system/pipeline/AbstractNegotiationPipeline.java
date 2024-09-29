/*
 *  Copyright (c) 2024 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.dsp.system.pipeline;

import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.dataspacetck.core.api.message.MessageSerializer;
import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.NegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageFunctions.createDspContext;

/**
 * Base pipeline functionality common to the consumer and provider pipelines.
 */
public abstract class AbstractNegotiationPipeline<P extends NegotiationPipeline<P>> implements NegotiationPipeline<P> {
    protected static final CountDownLatch NO_WAIT_LATCH = new CountDownLatch(0);

    protected CallbackEndpoint endpoint;
    protected Monitor monitor;
    protected long waitTime;

    protected List<Runnable> stages = new ArrayList<>();

    protected ContractNegotiation negotiation;

    /*
     Used by {@link #thenWait} methods to synchronize with the completion of a recorded expectXXX(..) method to avoid message interleaving.
     For example given:

     <pre>
         .expectOffer(offer -> consumerConnector.getConsumerNegotiationManager().handleProviderOffer(offer))
         .sendRequest(datasetId, offerId)
         .thenWaitForState(OFFERED)
         .expectAgreement(agreement -> consumerConnector.getConsumerNegotiationManager().handleAgreement(agreement))
     </pre>

     When sendRequest is executed and the pipeline waits for the OFFERED state, it must ensure that after OFFERED is set the
     expectOffer() runnable has completed before executing the next step, expectAgreement(). Otherwise, the send agreement message
     could arrive at the provider before the expectOffer() runnable is completed, which closes the HTTP socket the provider opened to
     send the offer.

     Every expectXXXX(..) method places a latch on the deque which is then popped by the subsequent {@link #thenWait} method.
     The {@link #thenWait} method waits on the latch, which is released after the expectXXXX(..) method completes.
     */
    protected Deque<CountDownLatch> expectLatches = new ArrayDeque<>();

    public AbstractNegotiationPipeline(CallbackEndpoint endpoint, Monitor monitor, long waitTime) {
        this.endpoint = endpoint;
        this.waitTime = waitTime;
        this.monitor = monitor;
    }

    public P thenWaitForState(ContractNegotiation.State state) {
        return thenWait("state to transition to " + state, () -> state == negotiation.getState());
    }

    public P thenWait(String description, Callable<Boolean> condition) {
        var latch = expectLatches.isEmpty() ? NO_WAIT_LATCH : expectLatches.pop();
        stages.add(() -> {
            try {
                try {
                    if (!latch.await(waitTime, SECONDS)) {
                        throw new RuntimeException("Timeout waiting for " + description);
                    }
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    throw new RuntimeException("Interrupted while waiting for " + description, e);
                }
                await().atMost(waitTime, SECONDS).until(condition);
                monitor.debug("Done waiting for " + description);
            } catch (ConditionTimeoutException e) {
                throw new AssertionError("Timeout waiting for " + description);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        //noinspection unchecked
        return (P) this;
    }

    public void execute() {
        stages.forEach(Runnable::run);
    }

    protected P addHandlerAction(String path, Consumer<Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(path, agreement -> {
                    action.accept((MessageSerializer.processJsonLd(agreement, createDspContext())));
                    endpoint.deregisterHandler(path);
                    latch.countDown();
                    return null;
                }));
        //noinspection unchecked
        return (P) this;
    }

    protected void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
