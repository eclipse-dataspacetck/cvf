package cvf.ids.system;

import cvf.core.api.system.CallbackEndpoint;
import cvf.core.spi.system.ServiceConfiguration;
import cvf.core.spi.system.ServiceResolver;
import cvf.core.spi.system.SystemConfiguration;
import cvf.core.spi.system.SystemLauncher;
import cvf.ids.system.api.client.NegotiationClient;
import cvf.ids.system.api.connector.Client;
import cvf.ids.system.api.connector.Connector;
import cvf.ids.system.api.mock.ProviderNegotiationMock;
import cvf.ids.system.api.pipeline.NegotiationPipeline;
import cvf.ids.system.client.NegotiationClientImpl;
import cvf.ids.system.mock.ProviderNegotiationMockImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static cvf.ids.system.api.pipeline.NegotiationPipeline.negotiationPipeline;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Instantiates and bootstraps an IDS test fixture.
 */
public class IdsSystemLauncher implements SystemLauncher {
    private static final String CVF_LOCAL_CONNECTOR = "cvf.ids.local.connector";
    private static final String CVF_THREAD_POOL = "cvf.ids.thread.pool";

    private ExecutorService executor;
    private boolean useLocalConnector;

    private Map<String, Connector> clientConnectors = new ConcurrentHashMap<>();
    private Map<String, Connector> providerConnectors = new ConcurrentHashMap<>();

    private Map<String, ProviderNegotiationMock> negotiationMocks = new ConcurrentHashMap<>();
    private Map<String, NegotiationClient> negotiationClients = new ConcurrentHashMap<>();

    @Override
    public void start(SystemConfiguration configuration) {
        executor = newFixedThreadPool(configuration.getPropertyAsInt(CVF_THREAD_POOL, 10));
        useLocalConnector = configuration.getPropertyAsBoolean(CVF_LOCAL_CONNECTOR, false);
    }

    @Override
    public <T> boolean providesService(Class<T> type) {
        return type.equals(NegotiationClient.class)
                || type.equals(Connector.class)
                || type.equals(ProviderNegotiationMock.class)
                || type.equals(NegotiationPipeline.class);
    }

    @Nullable
    @Override
    public <T> T getService(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        if (NegotiationPipeline.class.equals(type)) {
            return createPipeline(type, configuration, resolver);
        } else if (Connector.class.equals(type)) {
            return createConnector(type, configuration);
        } else if (ProviderNegotiationMock.class.equals(type)) {
            return createNegotiationMock(type, configuration.getScopeId());
        } else if (NegotiationClient.class.equals(type)) {
            return type.cast(createNegotiationClient(configuration.getScopeId()));
        }
        return null;
    }

    private <T> T createPipeline(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        var scopeId = configuration.getScopeId();
        var negotiationClient = createNegotiationClient(scopeId);
        var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
        var consumerConnector = clientConnectors.computeIfAbsent(scopeId, k -> new Connector());
        return type.cast(negotiationPipeline(negotiationClient, callbackEndpoint, consumerConnector));
    }

    private <T> T createNegotiationMock(Class<T> type, String scopeId) {
        return type.cast(negotiationMocks.computeIfAbsent(scopeId, k -> {
            var connector = providerConnectors.computeIfAbsent(scopeId, k2 -> new Connector());
            return new ProviderNegotiationMockImpl(connector.getProviderNegotiationManager(), executor);
        }));
    }

    private <T> T createConnector(Class<T> type, ServiceConfiguration configuration) {
        var scopeId = configuration.getScopeId();
        if (configuration.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(Client.class))) {
            return type.cast(clientConnectors.computeIfAbsent(scopeId, k -> new Connector()));
        }
        return type.cast(providerConnectors.computeIfAbsent(scopeId, k -> new Connector()));
    }

    private NegotiationClient createNegotiationClient(String scopeId) {
        return negotiationClients.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                return new NegotiationClientImpl(providerConnectors.computeIfAbsent(scopeId, k2 -> new Connector()));
            }
            return new NegotiationClientImpl();
        });
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

}
