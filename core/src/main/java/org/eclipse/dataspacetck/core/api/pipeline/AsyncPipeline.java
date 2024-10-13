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

import java.util.concurrent.Callable;

/**
 * Constructs a set of asynchronous message steps with a system under test.
 */
public interface AsyncPipeline<P extends AsyncPipeline<P>> {

    /**
     * Waits for the condition.
     */
    P thenWait(String description, Callable<Boolean> condition);

    /**
     * Executes a runnable when the step is active.
     */
    P then(Runnable runnable);

    /**
     * Executes the pipeline actions.
     */
    void execute();

}
