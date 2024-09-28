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

package org.eclipse.dataspacetck.dsp.verification.cn;

import org.eclipse.dataspacetck.core.api.system.Inject;
import org.eclipse.dataspacetck.core.api.verification.AbstractVerificationTest;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.Provider;
import org.eclipse.dataspacetck.dsp.system.api.mock.ConsumerNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.ConsumerNegotiationPipeline;
import org.junit.jupiter.api.Tag;

/**
 * Base class for verifying a connector in the consumer role.
 */
@Tag("dsp-cn")
public abstract class AbstractContractNegotiationConsumerTest extends AbstractVerificationTest {

    @Inject
    @Provider
    protected Connector providerConnector;

    @Inject
    protected ConsumerNegotiationPipeline negotiationPipeline;

    @Inject
    protected ConsumerNegotiationMock negotiationMock;

}
