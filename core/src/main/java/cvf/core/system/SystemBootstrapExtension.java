package cvf.core.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import cvf.core.api.system.CallbackEndpoint;
import cvf.core.spi.system.ServiceConfiguration;
import cvf.core.spi.system.SystemConfiguration;
import cvf.core.spi.system.SystemLauncher;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static cvf.core.api.message.SerializationFunctions.serialize;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

/**
 *
 */
public class SystemBootstrapExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource, ParameterResolver {
    private static final String CVF_CALLBACK_ADDRESS = "cvf.callback.address";
    private static final String CVF_LAUNCHER = "cvf.launcher";
    private static final ExtensionContext.Namespace CALLBACK_NAMESPACE = ExtensionContext.Namespace.create(new Object());

    private static boolean started;

    private static SystemLauncher launcher;
    private static DispatchingHandler dispatchingHandler;
    private static HttpServer server;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (started) {
            return;
        }
        started = true;
        context.getRoot().getStore(GLOBAL).put(SystemBootstrapExtension.class.getName() + "-initialized", this);

        launcher = initializeLauncher(context);
        var configuration = SystemConfiguration.Builder.newInstance()
                .propertyDelegate(k -> context.getConfigurationParameter(k).orElse(null))
                .build();

        launcher.start(configuration);

        dispatchingHandler = new DispatchingHandler();
        server = initializeCallbackServer(dispatchingHandler, context);
        server.start();
    }

    @Override
    public void close() {
        if (launcher != null) {
            launcher.close();
        }
        if (server != null) {
            server.stop(0);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();
        return launcher.providesService(type) || type.equals(CallbackEndpoint.class) || launcher.providesService(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();
        if (type.equals(CallbackEndpoint.class)) {
            var endpoint = attachCallbackEndpoint(dispatchingHandler, extensionContext);
            extensionContext.getStore(CALLBACK_NAMESPACE).put("callback", endpoint);
            return endpoint;
        } else {
            var tags = extensionContext.getTags();
            var configuration = ServiceConfiguration.Builder.newInstance()
                    .tags(tags)
                    .propertyDelegate(k -> extensionContext.getConfigurationParameter(k).orElse(null))
                    .build();
            var id = extensionContext.getUniqueId();
            var service = launcher.getService(type, configuration, id);
            if (service != null) {
                return service;
            }
        }
        throw new ParameterResolutionException("Unsupported parameter type");
    }

    private CallbackEndpoint attachCallbackEndpoint(DispatchingHandler dispatchingHandler, ExtensionContext context) {
        var endpointBuilder = DefaultCallbackEndpoint.Builder.newInstance();
        endpointBuilder.address(context.getConfigurationParameter(CVF_CALLBACK_ADDRESS).orElse("http://localhost:8083")); // xcv
        endpointBuilder.listener(dispatchingHandler::deregisterEndpoint);

        var endpoint = endpointBuilder.build();
        dispatchingHandler.registerEndpoint(endpoint);
        return endpoint;
    }

    private SystemLauncher initializeLauncher(ExtensionContext context) {
        var launcherClass = context.getConfigurationParameter(CVF_LAUNCHER);

        if (launcherClass.isEmpty()) {
            return new NoOpSystemLauncher();
        } else {
            try {
                return (SystemLauncher) getClass().getClassLoader().loadClass(launcherClass.get()).getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Unable to create Launcher class: " + launcherClass, e);
            }
        }
    }

    private HttpServer initializeCallbackServer(HttpHandler rootHandler, ExtensionContext context) {
        try {
            server = HttpServer.create(new InetSocketAddress(8083), 0);        // XCV align with callback address
            server.createContext("/", rootHandler);
            return server;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class DispatchingHandler implements HttpHandler {
        private ObjectMapper mapper = new ObjectMapper();

        private Queue<DefaultCallbackEndpoint> endpoints = new ConcurrentLinkedQueue<>();

        void registerEndpoint(DefaultCallbackEndpoint endpoint) {
            endpoints.add(endpoint);
        }

        void deregisterEndpoint(DefaultCallbackEndpoint endpoint) {
            endpoints.remove(endpoint);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            var path = exchange.getRequestURI().getPath();
            for (var endpoint : endpoints) {
                if (endpoint.handlesPath(path)) {
                    var message = mapper.readValue(exchange.getRequestBody(), Object.class);
                    var response = endpoint.apply(path, message);
                    if (response == null) {
                        exchange.sendResponseHeaders(200, 0);
                    } else {
                        var serialized = serialize(response).getBytes();
                        exchange.sendResponseHeaders(200, serialized.length);
                        var responseBody = exchange.getResponseBody();
                        responseBody.write(serialized);
                        responseBody.close();
                    }
                    return;
                }
            }
            throw new IllegalArgumentException("Callback path not registered: " + path);
        }
    }

    private static class NoOpSystemLauncher implements SystemLauncher {

        @Override
        public void start(SystemConfiguration configuration) {
        }

    }

}
