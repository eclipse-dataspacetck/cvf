package cvf.ids.system.api.connector;

/**
 * Implements a simple, in-memory connector that supports control-plane operations for testing.
 */
public class Connector {
    private ProviderNegotiationManager providerNegotiationManager;
    private ConsumerNegotiationManager consumerNegotiationManager;

    public ProviderNegotiationManager getProviderNegotiationManager() {
        return providerNegotiationManager;
    }

    public ConsumerNegotiationManager getConsumerNegotiationManager() {
        return consumerNegotiationManager;
    }

    public Connector() {
        consumerNegotiationManager = new ConsumerNegotiationManager();
        providerNegotiationManager = new ProviderNegotiationManager();
    }
}
