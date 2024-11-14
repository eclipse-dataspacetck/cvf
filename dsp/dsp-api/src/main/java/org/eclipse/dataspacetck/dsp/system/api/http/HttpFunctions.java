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

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serialize;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serializePlainJson;

/**
 * Utility methods for HTTP requests.
 */
public class HttpFunctions {
    private static Interceptor authorizationInterceptor = chain -> chain.proceed(chain.request());

    public static void registerAuthorizationInterceptor(String authorizationHeader) {
        authorizationInterceptor = chain -> {
            var request = chain.request();
            var authenticatedRequest = request.newBuilder()
                            .header("Authorization", authorizationHeader).build();
            return chain.proceed(authenticatedRequest);
        };
    }

    public static Response postJson(String url, Object message) {
        return postJson(url, message, false);
    }

    public static Response postJson(String url, Object message, boolean expectError) {
        return postJson(url, message, expectError, false);
    }

    public static Response postJson(String url, Object message, boolean expectError, boolean plain) {
        var serialized = plain ? serializePlainJson(message) : serialize(message);
        var requestBody = RequestBody.create(serialized, MediaType.get("application/json"));
        var httpRequest = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        var httpClient = new OkHttpClient.Builder().addInterceptor(authorizationInterceptor).build();
        try {
            var response = httpClient.newCall(httpRequest).execute();
            if (404 == response.code()) {
                throw new AssertionError("Unexpected 404 received for request: " + url);
            } else if (!response.isSuccessful()) {
                if (response.code() < 400 || response.code() >= 500 || !expectError) {
                    throw new AssertionError("Unexpected response code: " + response.code());
                }
            }
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Response getJson(String url) {
        var httpRequest = new Request.Builder()
                .url(url)
                .get()
                .build();

        var httpClient = new OkHttpClient.Builder().addInterceptor(authorizationInterceptor).build();
        try {
            var response = httpClient.newCall(httpRequest).execute();
            if (404 == response.code()) {
                throw new AssertionError("Unexpected 404 received for request: " + url);
            } else if (!response.isSuccessful()) {
                throw new AssertionError("Unexpected response code: " + response.code());
            }
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpFunctions() {
    }
}
