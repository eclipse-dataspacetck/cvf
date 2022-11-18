package cvf.core.spi.system;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Initializes and interfaces with the system being verified.
 */
public interface SystemLauncher {

    /**
     * Returns the collection of supported client interfaces.
     */
    Set<Class<?>> clientTypes();

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
     * Creates a proxy to the system being verified of the given type.
     */
    <T> T createClient(Class<T> type, ClientConfiguration configuration, String scopeId);


    /**
     * Returns a service of the given type or null for the provided scope. Some services may not be available until {@link #start(SystemConfiguration)} ()} is invoked.
     */
    @Nullable
    default <T> T getService(Class<T> type, String scopeId) {
        return null;
    }

    /**
     * Returns true if the launcher can provide a service of the given type.
     */
    default <T> boolean providesService(Class<T> type) {
        return false;
    }
}
