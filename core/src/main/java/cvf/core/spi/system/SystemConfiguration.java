package cvf.core.spi.system;

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
