/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.dataspacetck.dsp.system.api.message;

/**
 * ODRL constants.
 */
public interface OdrlConstants {
    String ODRL_NAMESPACE = "http://www.w3.org/ns/odrl/2/";
    String ODRL_NAMESPACE_KEY = "odrl";
    String ODRL_NAMESPACE_PREFIX = ODRL_NAMESPACE_KEY + ":";
    String ODRL_AGREEMENT_TYPE = ODRL_NAMESPACE_PREFIX + "Agreement";
    String ODRL_OFFER_TYPE = ODRL_NAMESPACE_PREFIX + "Offer";
    String ODRL_PROPERTY_ACTION = ODRL_NAMESPACE_PREFIX + "action";
    String ODRL_USE = ODRL_NAMESPACE_PREFIX + "use";
    String ODRL_PROPERTY_CONSTRAINTS = ODRL_NAMESPACE_PREFIX + "constraints";
    String ODRL_PROPERTY_PERMISSION = ODRL_NAMESPACE_PREFIX + "permission";
}
