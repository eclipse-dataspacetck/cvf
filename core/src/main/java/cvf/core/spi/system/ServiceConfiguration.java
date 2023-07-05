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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Configuration used to create a system service.
 */
public class ServiceConfiguration extends AbstractConfiguration {
    private Set<String> tags = new HashSet<>();
    private List<Annotation> annotations = new ArrayList<>();
    private String scopeId;

    public String getScopeId() {
        return scopeId;
    }

    /**
     * Returns associated JUnit test tags.
     */
    public Set<String> getTags() {
        return tags;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    private ServiceConfiguration() {
        super();
    }

    public static class Builder extends AbstractConfiguration.Builder<Builder> {
        private ServiceConfiguration configuration;

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder tags(Set<String> tags) {
            configuration.tags.addAll(tags);
            return this;
        }

        public Builder annotations(Annotation[] annotations) {
            this.configuration.annotations.addAll(asList(annotations));
            return this;
        }

        public Builder scopeId(String id) {
            configuration.scopeId = id;
            return this;
        }

        public ServiceConfiguration build() {
            return configuration;
        }

        @Override
        protected AbstractConfiguration getConfiguration() {
            return configuration;
        }

        private Builder() {
            super();
            configuration = new ServiceConfiguration();
        }

    }
}
