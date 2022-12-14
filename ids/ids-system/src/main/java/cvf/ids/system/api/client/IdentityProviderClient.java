package cvf.ids.system.api.client;

import java.util.Map;

/**
 * Proxy to an identity provider trusted by the client and provider.
 */
public interface IdentityProviderClient {

    /**
     * Returns security headers for the test key.
     */
    Map<String, String> getSecurityHeaders(String key);

}
