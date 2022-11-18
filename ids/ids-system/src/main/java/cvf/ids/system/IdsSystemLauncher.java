package cvf.ids.system;

import cvf.core.spi.system.ClientConfiguration;
import cvf.core.spi.system.SystemConfiguration;
import cvf.core.spi.system.SystemLauncher;
import cvf.ids.system.api.client.NegotiationClient;
import cvf.ids.system.api.connector.Connector;
import cvf.ids.system.api.mock.ProviderNegotiationMock;
import cvf.ids.system.client.NegotiationClientImpl;
import cvf.ids.system.mock.ProviderNegotiationMockImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Instantiates and bootstraps an IDS test fixture.
 */
public class IdsSystemLauncher implements SystemLauncher {
    private static final String CVF_LOCAL_CONNECTOR = "cvf.ids.local.connector";
    private static final String CVF_THREAD_POOL = "cvf.ids.thread.pool";

    private static final Set<Class<?>> TYPES = Set.of(NegotiationClient.class);

    private ExecutorService executor;
    private boolean useLocalConnector;

    private Map<String, Connector> systemConnectors = new ConcurrentHashMap<>();
    private Map<String, ProviderNegotiationMock> negotiationMocks = new ConcurrentHashMap<>();

    @Override
    public Set<Class<?>> clientTypes() {
        return TYPES;
    }

    @Override
    public void start(SystemConfiguration configuration) {
        executor = newFixedThreadPool(configuration.getPropertyAsInt(CVF_THREAD_POOL, 10));
        useLocalConnector = configuration.getPropertyAsBoolean(CVF_LOCAL_CONNECTOR, false);
    }

    @Override
    public <T> T createClient(Class<T> type, ClientConfiguration configuration, String scopeId) {
        requireNonNull(type);
        requireNonNull(configuration);
        if (!NegotiationClient.class.equals(type)) {
            throw new IllegalArgumentException("Unsupported client type: " + type.getName());
        }
        if (useLocalConnector) {
            return type.cast(new NegotiationClientImpl(systemConnectors.computeIfAbsent(scopeId, k2 -> new Connector())));
        }
        return type.cast(new NegotiationClientImpl());
    }

    @Nullable
    @Override
    public <T> T getService(Class<T> type, String scopeId) {
        if (Connector.class.equals(type)) {
            return type.cast(systemConnectors.computeIfAbsent(scopeId, k -> new Connector()));
        } else if (ProviderNegotiationMock.class.equals(type)) {
            return type.cast(negotiationMocks.computeIfAbsent(scopeId, k -> {
                var connector = systemConnectors.computeIfAbsent(scopeId, k2 -> new Connector());
                return new ProviderNegotiationMockImpl(connector.getProviderNegotiationManager(), executor);
            }));
        }
        return null;
    }

    @Override
    public <T> boolean providesService(Class<T> type) {
        return type.equals(Connector.class) || type.equals(ProviderNegotiationMock.class);
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

}
