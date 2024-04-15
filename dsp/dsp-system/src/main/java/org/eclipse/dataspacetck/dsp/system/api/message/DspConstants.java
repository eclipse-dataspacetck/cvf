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

package org.eclipse.dataspacetck.dsp.system.api.message;

/**
 * Message constants.
 */
public interface DspConstants {

    String DSP_NAMESPACE = "https://w3id.org/dspace/v0.8/";

    String DSP_NAMESPACE_KEY = "dspace";

    String DSP_NAMESPACE_PREFIX = DSP_NAMESPACE_KEY + ":";

    String ODRL_NAMESPACE = "http://www.w3.org/ns/odrl/2/";

    String CONTEXT = "@context";

    String ID = "@id";

    String TYPE = "@type";

}
