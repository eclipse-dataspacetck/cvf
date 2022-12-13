package cvf.ids;

import java.util.UUID;

/**
 * Interface to the environment setup for contract negotiation.
 */
public class NegotiationSetup {

    public static String getDatasetId(String key) {
        return System.getProperty(key, UUID.randomUUID().toString());
    }

    public static String getOfferId(String key) {
        return System.getProperty(key, UUID.randomUUID().toString());
    }

    private NegotiationSetup() {
    }
}
