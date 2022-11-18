package cvf.ids.system;

import cvf.core.spi.system.ServiceConfiguration;
import cvf.core.spi.system.SystemConfiguration;
import cvf.core.spi.system.SystemLauncher;
import cvf.ids.system.api.client.NegotiationClient;
import cvf.ids.system.api.connector.Connector;
import cvf.ids.system.api.mock.ProviderNegotiationMock;
import cvf.ids.system.client.NegotiationClientImpl;
import cvf.ids.system.mock.ProviderNegotiationMockImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Instantiates and bootstraps an IDS test fixture.
 */
public class IdsSystemLauncher implements SystemLauncher {
    private static final String CVF_LOCAL_CONNECTOR = "cvf.ids.local.connector";
    private static final String CVF_THREAD_POOL = "cvf.ids.thread.pool";

    private ExecutorService executor;
    private boolean useLocalConnector;

    private Map<String, Connector> systemConnectors = new ConcurrentHashMap<>();
    private Map<String, ProviderNegotiationMock> negotiationMocks = new ConcurrentHashMap<>();
    private Map<String, NegotiationClient> negotiationClients = new ConcurrentHashMap<>();

    @Override
    public <T> boolean providesService(Class<T> type) {
        return type.equals(NegotiationClient.class) || type.equals(Connector.class) || type.equals(ProviderNegotiationMock.class);
    }

    @Override
    public void start(SystemConfiguration configuration) {
        executor = newFixedThreadPool(configuration.getPropertyAsInt(CVF_THREAD_POOL, 10));
        useLocalConnector = configuration.getPropertyAsBoolean(CVF_LOCAL_CONNECTOR, false);
    }

    @Nullable
    @Override
    public <T> T getService(Class<T> type, ServiceConfiguration configuration, String scopeId) {
        if (Connector.class.equals(type)) {
            return type.cast(systemConnectors.computeIfAbsent(scopeId, k -> new Connector()));
        } else if (ProviderNegotiationMock.class.equals(type)) {
            return type.cast(negotiationMocks.computeIfAbsent(scopeId, k -> {
                var connector = systemConnectors.computeIfAbsent(scopeId, k2 -> new Connector());
                return new ProviderNegotiationMockImpl(connector.getProviderNegotiationManager(), executor);
            }));
        } else if (NegotiationClient.class.equals(type)) {
            return type.cast(createNegotiationClient(scopeId));
        }
        return null;
    }

    private NegotiationClient createNegotiationClient(String scopeId) {
        return negotiationClients.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                return new NegotiationClientImpl(systemConnectors.computeIfAbsent(scopeId, k2 -> new Connector()));
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
