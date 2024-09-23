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

package org.eclipse.dataspacetck.core.system;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.system.ServiceConfiguration;
import org.eclipse.dataspacetck.core.spi.system.SystemConfiguration;
import org.eclipse.dataspacetck.core.spi.system.SystemLauncher;
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
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Boolean.parseBoolean;
import static org.eclipse.dataspacetck.core.api.system.SystemsConstants.TCK_CALLBACK_ADDRESS;
import static org.eclipse.dataspacetck.core.api.system.SystemsConstants.TCK_DEFAULT_CALLBACK_ADDRESS;
import static org.eclipse.dataspacetck.core.api.system.SystemsConstants.TCK_LAUNCHER;
import static org.eclipse.dataspacetck.core.system.ConfigFunctions.propertyOrEnv;
import static org.eclipse.dataspacetck.core.system.ConsoleMonitor.ANSI_PROPERTY;
import static org.eclipse.dataspacetck.core.system.ConsoleMonitor.DEBUG_PROPERTY;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class SystemBootstrapExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver, ExtensionContext.Store.CloseableResource {

    private static final ExtensionContext.Namespace CALLBACK_NAMESPACE = org.junit.jupiter.api.extension.ExtensionContext.Namespace.create(new Object());

    private static boolean started;

    private String callbackHost;
    private int callbackPort;
    private static SystemLauncher launcher;
    private static DispatchingHandler dispatchingHandler;
    private static HttpServer server;
    private ExecutorService executorService;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (started) {
            return;
        }
        started = true;
        context.getRoot().getStore(GLOBAL).put(SystemBootstrapExtension.class.getName() + "-initialized", this);

        launcher = initializeLauncher(context);

        var ansi = parseBoolean(context.getConfigurationParameter(ANSI_PROPERTY).orElse(propertyOrEnv(ANSI_PROPERTY, "true")));
        var debug = parseBoolean(context.getConfigurationParameter(DEBUG_PROPERTY).orElse(propertyOrEnv(DEBUG_PROPERTY, "false")));
        var callbackAddress = URI.create(context.getConfigurationParameter(TCK_CALLBACK_ADDRESS).orElse(propertyOrEnv(TCK_CALLBACK_ADDRESS, TCK_DEFAULT_CALLBACK_ADDRESS)));

        this.callbackHost = callbackAddress.getHost();
        this.callbackPort = callbackAddress.getPort();

        var configuration = SystemConfiguration.Builder.newInstance()
                .propertyDelegate(k -> context.getConfigurationParameter(k).orElse(propertyOrEnv(k, null)))
                .monitor(new ConsoleMonitor(debug, ansi))
                .build();

        launcher.start(configuration);

        dispatchingHandler = new DispatchingHandler();
        executorService = Executors.newFixedThreadPool(1);
        server = initializeCallbackServer(dispatchingHandler, executorService);
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
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        var type = parameterContext.getParameter().getType();
        return launcher.providesService(type) || type.equals(CallbackEndpoint.class);
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
                .propertyDelegate(k -> context.getConfigurationParameter(k).orElse(propertyOrEnv(k, null)))
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
        endpointBuilder.address(context.getConfigurationParameter(TCK_CALLBACK_ADDRESS)
                .orElse(propertyOrEnv(TCK_CALLBACK_ADDRESS, TCK_DEFAULT_CALLBACK_ADDRESS)));
        endpointBuilder.listener(dispatchingHandler::deregisterEndpoint);

        var endpoint = endpointBuilder.build();
        dispatchingHandler.registerEndpoint(endpoint);
        return endpoint;
    }

    private SystemLauncher initializeLauncher(ExtensionContext context) {
        var launcherClass = context.getConfigurationParameter(TCK_LAUNCHER).orElse(propertyOrEnv(TCK_LAUNCHER, null));

        if (launcherClass == null) {
            return new NoOpSystemLauncher();
        } else {
            try {
                return (SystemLauncher) getClass().getClassLoader().loadClass(launcherClass).getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Unable to create Launcher class: " + launcherClass, e);
            }
        }
    }

    private HttpServer initializeCallbackServer(HttpHandler rootHandler, ExecutorService executorService) {
        try {
            server = HttpServer.create(new InetSocketAddress(callbackHost, callbackPort), 0);
            server.createContext("/", rootHandler);
            server.setExecutor(executorService);
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
                    var response = endpoint.apply(path, exchange.getRequestBody());
                    if (response == null) {
                        exchange.sendResponseHeaders(200, 0);
                    } else {
                        var bytes = response.getBytes();
                        exchange.sendResponseHeaders(200, bytes.length);
                        var responseBody = exchange.getResponseBody();
                        responseBody.write(bytes);
                        responseBody.close();
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
