package cvf.core.spi.system;

import org.jetbrains.annotations.Nullable;

/**
 * Resolves services.
 */
@FunctionalInterface
public interface ServiceResolver {

    /**
     * Resolves an instance of the type or null if not found.
     */
    @Nullable
    Object resolve(Class<?> type, ServiceConfiguration configuration);
}
