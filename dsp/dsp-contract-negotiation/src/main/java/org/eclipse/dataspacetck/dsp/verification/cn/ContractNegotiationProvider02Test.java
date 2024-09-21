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

package org.eclipse.dataspacetck.dsp.verification.cn;

import org.eclipse.dataspacetck.core.api.system.MandatoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.TERMINATED;

@Tag("base-compliance")
@DisplayName("CN_02: Provider test scenarios")
public class ContractNegotiationProvider02Test extends AbstractContractNegotiationProviderTest {

    @MandatoryTest
    @DisplayName("CN:02-01: Verify contract request, provider terminated")
    public void cn_02_01() {

        negotiationMock.recordContractRequestedAction(ProviderActions::terminate);

        negotiationPipeline
                .sendRequest(datasetId, offerId)
                .expectTermination()
                .thenWaitForState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN:02-02: Verify contract request, consumer terminated")
    public void cn_02_02() {

        negotiationMock.recordContractRequestedAction(ProviderActions::terminate);

        negotiationPipeline
                .sendRequest(datasetId, offerId)
                .sendTermination()
                .thenVerifyProviderState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }


}
