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

package org.eclipse.dataspacetck.core.system;

import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;

/**
 * Implements a callback endpoint.
 * <p>
 * Deserialized messages from incoming transports such as HTTP are dispatched to a registered handler through this endpoint by calling {@link #apply(String, InputStream)}.
 */
public class DefaultCallbackEndpoint implements CallbackEndpoint, BiFunction<String, InputStream, String>, ExtensionContext.Store.CloseableResource {

    @FunctionalInterface
    public interface LifecycleListener {
        void onClose(DefaultCallbackEndpoint endpoint);
    }

    private String address;
    private List<LifecycleListener> listeners = new ArrayList<>();
    private Map<String, Function<InputStream, String>> handlers = new HashMap<>();


    @Override
    public String getAddress() {
        return address;
    }

    public boolean handlesPath(String path) {
        return lookupHandler(stripTrailingSlash(path)).isPresent();
    }

    @Override
    public String apply(String path, InputStream message) {
        //noinspection OptionalGetWithoutIsPresent
        return lookupHandler(stripTrailingSlash(path)).get().apply(message);
    }

    @Override
    public void registerHandler(String path, Function<InputStream, String> handler) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        path = stripTrailingSlash(path);
        handlers.put(path, handler);
    }

    @Override
    public void deregisterHandler(String path) {
        handlers.remove(path);
    }

    private DefaultCallbackEndpoint() {
    }

    @Override
    public void close() {
        listeners.forEach(l -> l.onClose(this));
    }

    @NotNull
    private String stripTrailingSlash(String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public static class Builder {
        private DefaultCallbackEndpoint endpoint;

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder address(String address) {
            endpoint.address = address;
            return this;
        }

        public Builder listener(LifecycleListener listener) {
            endpoint.listeners.add(listener);
            return this;
        }

        public DefaultCallbackEndpoint build() {
            requireNonNull(endpoint.address);
            return endpoint;
        }

        private Builder() {
            endpoint = new DefaultCallbackEndpoint();
        }
    }

    /**
     * Matches the path based on the regular expression.
     */
    private Optional<Function<InputStream, String>> lookupHandler(String expression) {
        return handlers.entrySet()
                .stream()
                .filter(entry -> compile(entry.getKey()).matcher(expression).matches())
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
