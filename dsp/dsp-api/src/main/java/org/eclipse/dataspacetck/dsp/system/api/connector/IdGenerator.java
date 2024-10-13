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

package org.eclipse.dataspacetck.dsp.system.api.connector;

/**
 * Handles ID generation
 */
public class IdGenerator {

    public static final String OFFER = "offer";

    public static String offerIdFromDatasetId(String datasetId) {
        return OFFER + datasetId;
    }

    public static String datasetIdFromOfferId(String offerId) {
        if (!offerId.startsWith(OFFER)) {
            throw new IllegalArgumentException("Offer ID must start with " + OFFER);
        }
        return offerId.substring(OFFER.length());
    }
}
