package cvf.core.spi.system;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration used to create a system client proxy.
 */
public class ServiceConfiguration extends AbstractConfiguration {
    private Set<String> tags = new HashSet<>();

    /**
     * Returns associated JUnit test tags.
     */
    public Set<String> getTags() {
        return tags;
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
