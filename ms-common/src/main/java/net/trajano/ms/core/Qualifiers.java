package net.trajano.ms.core;

import net.trajano.ms.spi.CacheNames;

/**
 * Unclassified qualifiers used by the application.
 *
 * @author Archimedes Trajano
 */
public final class Qualifiers {

    /**
     * JWKS Cache Name.
     * 
     * @deprecated use {@link net.trajano.ms.spi.CacheNames#JWKS}
     */
    @Deprecated
    public static final String JWKS_CACHE = CacheNames.JWKS;

    /**
     * Request ID HTTP Header.
     */
    public static final String REQUEST_ID = "X-Request-ID";

    /**
     * Roles claim name. The claim is expected to be in a string list format.
     */
    public static final String ROLES = "roles";

    private Qualifiers() {

    }
}
