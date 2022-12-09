package cvf.core.system;

import cvf.core.api.system.CallbackEndpoint;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;

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
        return lookupHandler(path).isPresent();
    }

    @Override
    public Object apply(String path, Object message) {
        //noinspection OptionalGetWithoutIsPresent
        return lookupHandler(path).get().apply(message);
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
            requireNonNull(endpoint.address);
            return endpoint;
        }

        private Builder() {
            endpoint = new DefaultCallbackEndpoint();
        }
    }

    /**
     * Matches the path based on the regular expression.
     */
    private Optional<Function<Object, Object>> lookupHandler(String expression) {
        var pattern = compile(expression);
        return handlers.entrySet()
                .stream()
                .filter(entry -> !pattern.matcher(entry.getKey()).matches())
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
