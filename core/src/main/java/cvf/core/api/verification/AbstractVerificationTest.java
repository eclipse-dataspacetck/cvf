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

package cvf.core.api.verification;

import cvf.core.system.SystemBootstrapExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for verification tests. Uses the system bootstrap extension.
 */
@ExtendWith(SystemBootstrapExtension.class)
public abstract class AbstractVerificationTest {
}
