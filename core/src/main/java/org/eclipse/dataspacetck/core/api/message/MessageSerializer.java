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

package org.eclipse.dataspacetck.core.api.message;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.apicatalog.jsonld.JsonLd.compact;
import static com.apicatalog.jsonld.JsonLd.expand;

/**
 * Provides a configured {@link ObjectMapper} for serializing and deserializing JSON-LD messages.
 */
public class MessageSerializer {
    public static final JsonDocument EMPTY_CONTEXT = JsonDocument.of(JsonStructure.EMPTY_JSON_OBJECT);

    public static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JSONPModule());
        var module = new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
            }
        };
        MAPPER.registerModule(module);
    }

    public static String serialize(Object object) {
        try {
            var compacted = compact(JsonDocument.of(MAPPER.convertValue(object, JsonObject.class)), EMPTY_CONTEXT).get();
            return MAPPER.writeValueAsString(compacted);
        } catch (JsonProcessingException | JsonLdError e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> processJsonLd(InputStream stream, Map<String, Object> context) {
        try {
            return processJsonLd(MAPPER.readValue(stream, JsonObject.class), context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> processJsonLd(Map<String, Object> message, Map<String, Object> context) {
        return processJsonLd(MAPPER.convertValue(message, JsonObject.class), context);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> processJsonLd(JsonObject document, Map<String, Object> context) {
        try {
            var options = new JsonLdOptions();
            options.setExpandContext(MAPPER.convertValue(context, JsonObject.class));
            options.setCompactArrays(true);
            var jsonArray = expand(JsonDocument.of(document)).options(options).get();
            if (jsonArray.isEmpty()) {
                throw new AssertionError("Invalid Json document, expecting a non-empty array");
            }
            @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
            var expanded = jsonArray.get(0);
            return MAPPER.convertValue(expanded, Map.class);
        } catch (JsonLdError e) {
            throw new RuntimeException(e);
        }
    }

    private MessageSerializer() {
    }


}
