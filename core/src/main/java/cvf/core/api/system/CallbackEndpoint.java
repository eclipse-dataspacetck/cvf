package cvf.core.api.system;

import java.util.function.Function;

/**
 * An endpoint to receive asynchronous responses.
 */
public interface CallbackEndpoint {

    /**
     * The callback base address, for example <code>https://test.com</code>.
     */
    String getAddress();

    /**
     * Registers a response handler for the given callback path.
     */
    void registerHandler(String path, Function<Object, Object> consumer);

    /**
     * Deregisters a response handler.
     */
    void deregisterHandler(String path);

}
