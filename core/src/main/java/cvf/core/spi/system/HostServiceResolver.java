package cvf.core.spi.system;

import org.jetbrains.annotations.Nullable;

/**
 * Resolves host services
 */
@FunctionalInterface
public interface HostServiceResolver {

    /**
     * Resolves an instance of the type or null if not found.
     */
    @Nullable
    <T> T resolve(Class<T> service);
}
