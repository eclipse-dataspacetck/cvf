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

package org.eclipse.dataspacetck.dsp.system.api.http;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serialize;

/**
 * Utility methods for HTTP requests.
 */
public class HttpFunctions {

    public static Response postJson(String url, Object message) {
        var requestBody = RequestBody.create(serialize(message), MediaType.get("application/json"));
        var httpRequest = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        var httpClient = new OkHttpClient.Builder().build();
        try {
            var response = httpClient.newCall(httpRequest).execute();
            if (404 == response.code()) {
                throw new RuntimeException("Unexpected callback received: " + url);
            } else if (!response.isSuccessful()) {
                throw new RuntimeException("Unexpected response code: " + response.code());
            }
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpFunctions() {
    }
}
