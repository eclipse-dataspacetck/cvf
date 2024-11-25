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

package org.eclipse.dataspacetck.core.api.pipeline;

import org.awaitility.core.ConditionTimeoutException;
import org.eclipse.dataspacetck.core.api.message.MessageSerializer;
import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

/**
 * Base pipeline functionality.
 */
public abstract class AbstractAsyncPipeline<P extends AsyncPipeline<P>> implements AsyncPipeline<P> {
    protected static final CountDownLatch NO_WAIT_LATCH = new CountDownLatch(0);

    protected CallbackEndpoint endpoint;
    protected Monitor monitor;
    protected long waitTime;
    protected Supplier<Map<String, Object>> context;

    protected List<Runnable> stages = new ArrayList<>();

    /*
     Used by {@link #thenWait} methods to synchronize with the completion of a recorded expectXXX(..) method to avoid message interleaving.
     For example given:

     <pre>
         .expectMethod1(data -> doSomething(data))
         .sendRequest(id)
         .thenWait("Waiting for condition", condition)
         .expectMethod2(data -> doSomethingElse(data))
     </pre>

     When sendRequest is executed and the pipeline waits using thenWait, it must ensure that the
     expectMethod1() runnable has completed before executing the next step, expectMethod2(). Otherwise, the expectMethod2
     message sent by the remote system could arrive before the expectMethod1() runnable is completed.

     Every expectXXXX(..) method places a latch on the deque which is then popped by the subsequent {@link #thenWait} method.
     The {@link #thenWait} method waits on the latch, which is released after the expectXXXX(..) method completes.
     */
    protected Deque<CountDownLatch> expectLatches = new ArrayDeque<>();

    public AbstractAsyncPipeline(CallbackEndpoint endpoint, Monitor monitor, long waitTime, Supplier<Map<String, Object>> context) {
        this.endpoint = endpoint;
        this.waitTime = waitTime;
        this.monitor = monitor;
        this.context = context;
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

    public P then(Runnable runnable) {
        stages.add(runnable);
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
                    action.accept((MessageSerializer.processJsonLd(agreement, context.get())));
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
