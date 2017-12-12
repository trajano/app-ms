package net.trajano.ms.core;

import net.trajano.ms.spi.CacheNames;
import net.trajano.ms.spi.MDCKeys;

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
     *
     * @deprecated use {@link net.trajano.ms.spi.MDCKeys#REQUEST_ID}
     */
    @Deprecated
    public static final String REQUEST_ID = MDCKeys.REQUEST_ID;

    /**
     * Roles claim name. The claim is expected to be in a string list format.
     */
    public static final String ROLES = "roles";

    private Qualifiers() {

    }
}
