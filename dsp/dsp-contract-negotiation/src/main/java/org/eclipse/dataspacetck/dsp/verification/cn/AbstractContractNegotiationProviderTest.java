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

import org.eclipse.dataspacetck.core.api.system.ConfigParam;
import org.eclipse.dataspacetck.core.api.system.Inject;
import org.eclipse.dataspacetck.core.api.verification.AbstractVerificationTest;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.Consumer;
import org.eclipse.dataspacetck.dsp.system.api.mock.ProviderNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.ProviderNegotiationPipeline;
import org.junit.jupiter.api.Tag;

import static java.util.UUID.randomUUID;

/**
 * Base class for verifying a connector in the provider role.
 */
@Tag("dsp-cn")
public abstract class AbstractContractNegotiationProviderTest extends AbstractVerificationTest {

    @Inject
    @Consumer
    protected Connector consumerConnector;

    @Inject
    protected ProviderNegotiationPipeline negotiationPipeline;

    @Inject
    protected ProviderNegotiationMock negotiationMock;

    @ConfigParam
    protected String offerId = randomUUID().toString();

    @ConfigParam
    protected String datasetId = randomUUID().toString();

}
