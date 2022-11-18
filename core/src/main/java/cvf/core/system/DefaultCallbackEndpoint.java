package cvf.core.system;

import cvf.core.api.system.CallbackEndpoint;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implements a callback endpoint.
 * <p>
 * Deserialized messages from incoming transports such as HTTP are dispatched to a registered handler through this endpoint by calling {@link #apply(String, Object)}.
 */
class DefaultCallbackEndpoint implements CallbackEndpoint, BiFunction<String, Object, Object>, ExtensionContext.Store.CloseableResource {

    @FunctionalInterface
    public interface LifecycleListener {
        void onClose(DefaultCallbackEndpoint endpoint);
    }

    private String address;
    private List<LifecycleListener> listeners = new ArrayList<>();
    private Map<String, Function<Object, Object>> handlers = new HashMap<>();


    @Override
    public String getAddress() {
        return address;
    }

    public boolean handlesPath(String path) {
        return handlers.containsKey(path);
    }

    @Override
    public Object apply(String path, Object message) {
        return handlers.get(path).apply(message);
    }

    @Override
    public void registerHandler(String path, Function<Object, Object> handler) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        handlers.put(path, handler);
    }

    @Override
    public void deregisterHandler(String path) {
        handlers.remove(path);
    }

    private DefaultCallbackEndpoint() {
    }

    @Override
    public void close() {
        listeners.forEach(l -> l.onClose(this));
    }

    public static class Builder {
        private DefaultCallbackEndpoint endpoint;

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder address(String address) {
            endpoint.address = address;
            return this;
        }

        public Builder listener(LifecycleListener listener) {
            endpoint.listeners.add(listener);
            return this;
        }

        public DefaultCallbackEndpoint build() {
            Objects.requireNonNull(endpoint.address);
            return endpoint;
        }

        private Builder() {
            endpoint = new DefaultCallbackEndpoint();
        }
    }

}
