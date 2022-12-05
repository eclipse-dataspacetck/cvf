package cvf.core.spi.system;

import org.jetbrains.annotations.Nullable;

/**
 * Initializes and interfaces with the system being verified.
 */
public interface SystemLauncher {

    /**
     * Performs required initialization and signals to the system that a test run will start.
     */
    void start(SystemConfiguration configuration);

    /**
     * Signals that the test run has completed and resources may be freed.
     */
    default void close() {
    }

    /**
     * Returns a service of the given type or null. Some services may not be available until {@link #start(SystemConfiguration)} ()} is invoked.
     */
    @Nullable
    default <T> T getService(Class<T> type, ServiceConfiguration configuration, HostServiceResolver resolver) {
        return null;
    }

    /**
     * Returns true if the launcher can provide a service of the given type.
     */
    default <T> boolean providesService(Class<T> type) {
        return false;
    }
}
