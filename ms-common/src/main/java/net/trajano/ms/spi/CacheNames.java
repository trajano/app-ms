package net.trajano.ms.spi;

/**
 * Cache names. These are used by the core API. The service implementation
 * should provide caches with these names.
 */
public final class CacheNames {

    /**
     * JWKS Cache Name.
     */
    public static final String JWKS = "jwks_cache";

    /**
     * Nonce Cache Name.
     */
    public static final String NONCE = "nonce";

    /**
     * Prevent instantiation of constants class.
     */
    private CacheNames() {

    }

}
