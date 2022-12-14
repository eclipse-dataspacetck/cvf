package cvf.ids.system.api.mock;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Mock identity provider.
 */
public interface IdentityProviderMock {

    /**
     * Records a supplier that will be used for all requests of the given key.
     */
    void record(String key, Supplier<Map<String, String>> supplier);

    /**
     * Records a supplier that will be used the specified times when the give key is requested.
     */
    void record(String key, Supplier<Map<String, String>> supplier, int times);

}
