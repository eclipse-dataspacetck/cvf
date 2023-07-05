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

package cvf.core.spi.system;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

/**
 * Configuration used to start a {@link SystemLauncher}.
 */
public abstract class AbstractConfiguration {
    protected Function<String, String> propertyDelegate = k -> null;
    protected Map<String, String> extensibleConfiguration = new HashMap<>();

    public String getPropertyAsString(String key, String defaultValue) {
        var value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    public int getPropertyAsInt(String key, int defaultValue) {
        var value = getProperty(key);
        return value != null ? parseInt(value) : defaultValue;
    }

    public boolean getPropertyAsBoolean(String key, boolean defaultValue) {
        var value = getProperty(key);
        return value != null ? parseBoolean(value) : defaultValue;
    }

    protected AbstractConfiguration() {
    }

    private String getProperty(String key) {
        var value = extensibleConfiguration.get(key);
        if (value != null) {
            return value;
        }
        return propertyDelegate.apply(key);
    }

    public abstract static class Builder<B extends Builder<?>> {

        @SuppressWarnings("unchecked")
        public B property(String key, String value) {
            getConfiguration().extensibleConfiguration.put(key, value);
            return (B) this;
        }


        @SuppressWarnings("unchecked")
        public B propertyDelegate(Function<String, String> delegate) {
            getConfiguration().propertyDelegate = delegate;
            return (B) this;
        }

        protected abstract AbstractConfiguration getConfiguration();

        protected Builder() {
        }
    }
}
