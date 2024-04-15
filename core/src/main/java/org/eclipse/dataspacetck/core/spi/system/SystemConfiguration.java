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

package org.eclipse.dataspacetck.core.spi.system;

/**
 * Configuration used to start a {@link SystemLauncher}.
 */
public class SystemConfiguration extends AbstractConfiguration {

    protected SystemConfiguration() {
    }

    public static class Builder extends AbstractConfiguration.Builder<Builder> {
        private SystemConfiguration configuration;

        public static Builder newInstance() {
            return new Builder();
        }

        public SystemConfiguration build() {
            return configuration;
        }

        @Override
        protected AbstractConfiguration getConfiguration() {
            return configuration;
        }

        private Builder() {
            configuration = new SystemConfiguration();
        }
    }
}
