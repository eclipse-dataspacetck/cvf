/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 *
 */

package cvf.core.system;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import cvf.core.api.system.CallbackEndpoint;
import cvf.core.spi.system.ServiceConfiguration;
import cvf.core.spi.system.SystemConfiguration;
import cvf.core.spi.system.SystemLauncher;
import cvf.core.system.injection.InstanceInjector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.JUnitException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.apicatalog.jsonld.JsonLd.compact;
import static cvf.core.api.message.MessageSerializer.EMPTY_CONTEXT;
import static cvf.core.api.message.MessageSerializer.MAPPER;
import static cvf.core.api.message.MessageSerializer.serialize;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class SystemBootstrapExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver, ExtensionContext.Store.CloseableResource {
    private static final String CVF_CALLBACK_ADDRESS = "cvf.callback.address";
    private static final String CVF_LAUNCHER = "cvf.launcher";


    private static final ExtensionContext.Namespace CALLBACK_NAMESPACE = org.junit.jupiter.api.extension.ExtensionContext.Namespace.create(new Object());

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
        server = initializeCallbackServer(dispatchingHandler);
        server.start();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        new InstanceInjector((service, configuration) ->
                launcher.getService(service, configuration, (t, c) -> resolveInHierarchy(t, c, context)), context).inject(context.getTestInstance().orElseThrow());
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
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();
        var service = resolve(type, context);
        if (service != null) {
            return service;
        }
        var tags = context.getTags();
        var id = context.getUniqueId();
        var configuration = ServiceConfiguration.Builder.newInstance()
                .tags(tags)
                .scopeId(id)
                .annotations(parameterContext.getParameter().getAnnotations())
                .propertyDelegate(k -> context.getConfigurationParameter(k).orElse(null))
                .build();
        service = launcher.getService(type, configuration, (t, c) -> resolve(t, context));
        if (service != null) {
            return service;
        }
        throw new ParameterResolutionException("Unsupported parameter type: " + type.getName());
    }

    @NotNull
    private Object resolveInHierarchy(Class<?> type, ServiceConfiguration configuration, ExtensionContext context) {
        var resolved = resolve(type, context);
        if (resolved != null) {
            return resolved;
        }
        resolved = launcher.getService(type, configuration, (t1, c2) -> resolve(type, context));
        if (resolved != null) {
            return resolved;
        }
        throw new JUnitException("Type not found for injected field: " + type.getName());
    }

    @Nullable
    private Object resolve(Class<?> type, ExtensionContext context) {
        if (type.equals(CallbackEndpoint.class)) {
            var endpoint = attachCallbackEndpoint(dispatchingHandler, context);
            context.getStore(CALLBACK_NAMESPACE).put("callback", endpoint);
            return type.cast(endpoint);
        }
        return null;
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

    private HttpServer initializeCallbackServer(HttpHandler rootHandler) {
        try {
            server = HttpServer.create(new InetSocketAddress(8083), 0);        // XCV align with callback address
            server.createContext("/", rootHandler);
            return server;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class DispatchingHandler implements HttpHandler {
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
                    try {
                        var compacted = compact(JsonDocument.of(exchange.getRequestBody()), EMPTY_CONTEXT);
                        var message = MAPPER.convertValue(compacted.get(), Object.class);
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
                    } catch (JsonLdError e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
            }
            exchange.sendResponseHeaders(404, 0);
        }
    }

    private static class NoOpSystemLauncher implements SystemLauncher {

        @Override
        public void start(SystemConfiguration configuration) {
        }

    }

}
