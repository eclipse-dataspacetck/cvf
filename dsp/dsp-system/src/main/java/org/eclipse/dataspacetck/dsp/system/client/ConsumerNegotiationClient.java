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

package org.eclipse.dataspacetck.dsp.system.client;

import java.util.Map;

/**
 * Proxy to the consumer connector being verified for contract negotiation.
 */
public interface ConsumerNegotiationClient {

    /**
     * Signals to the consumer connector to initiate a contract request.
     */
    void initiateRequest(String datasetId, String offerId);

    /**
     * Sends an offer to the consumer connector.
     */
    void contractOffer(Map<String, Object> offer, boolean expectError);

    /**
     * Sends an agreement to the consumer connector.
     */
    void contractAgreement(Map<String, Object> agreement);

    /**
     * Sends the finalized event to the consumer connector.
     */
    void finalize(Map<String, Object> event, boolean b);

    /**
     * Retrieves the negotiation from the provider.
     */
    Map<String, Object> getNegotiation(String processId);
}
