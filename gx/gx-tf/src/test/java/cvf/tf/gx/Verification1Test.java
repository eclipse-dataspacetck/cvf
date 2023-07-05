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

package cvf.tf.gx;

import cvf.core.api.system.CallbackEndpoint;
import cvf.sample.tf.gx.system.api.GxSystemClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("level1")
public class Verification1Test extends AbstractGxVerificationTest {

    @Test
    public void verifyStatement_01(GxSystemClient systemClient, CallbackEndpoint endpoint) {
    }

    @Test
    public void verifyStatement_02(GxSystemClient client, CallbackEndpoint endpoint) {
    }
}
