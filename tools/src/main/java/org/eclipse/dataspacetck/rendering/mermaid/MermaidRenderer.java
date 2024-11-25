/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.rendering.mermaid;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.eclipse.dataspacetck.rendering.spi.DiagramImageRenderer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MermaidRenderer implements DiagramImageRenderer {
    private static final String KROKI_URL = "https://kroki.io/mermaid/";
    private final String imageType;

    public MermaidRenderer(String imageType) {
        this.imageType = imageType;
    }

    @Override
    public InputStream render(String diagramString) {
        var client = new OkHttpClient();
        var body = RequestBody.create(diagramString, MediaType.parse("text/plain"));
        var request = new Request.Builder()
                .url(KROKI_URL + imageType)
                .post(body)
                .build();
        try (var response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return new ByteArrayInputStream(response.body().bytes());
            } else {
                throw new RuntimeException("Unexpected HTTP response code: %s, message: %s".formatted(response.code(), response.message()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
