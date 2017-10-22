package net.trajano.ms.authz.internal;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Entries stored inside the cache. The JWT is stored instead of a JwtClaims to
 * allow encrypted JWTs to be used. Only some meta data needs to be exposed.
 */
public class TokenCacheEntry implements
    Serializable {

    private static final long serialVersionUID = -1643672416756418254L;

    private final String accessToken;

    /**
     * Client ID that originate the request. They are going to be the expected
     * audience for the token.
     */
    private final String clientId;

    /**
     * Created on. {@link Date} is used because of
     * https://sonarcloud.io/organizations/default/rules#rule_key=squid%3AS3437
     */
    private final Date createdOn;

    /**
     * Expires on. A given JWT should have a total life time it can exist value,
     * this is what this value denotes. {@link Date} is used because of
     * https://sonarcloud.io/organizations/default/rules#rule_key=squid%3AS3437
     */
    private final Date expiresOn;

    private final String jwt;

    private final String refreshToken;

    public TokenCacheEntry(final String accessToken,
        final String refreshToken,
        final String jwt,
        final String clientId) {

        this(accessToken, refreshToken, jwt, clientId, null);
    }

    public TokenCacheEntry(final String accessToken,
        final String refreshToken,
        final String jwt,
        final String clientId,
        final Instant expiresOn) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.jwt = jwt;
        this.clientId = clientId;
        this.expiresOn = Date.from(expiresOn);
        createdOn = Date.from(Instant.now());
    }

    public String getAccessToken() {

        return accessToken;
    }

    public String getClientId() {

        return clientId;
    }

    public Instant getCreatedOn() {

        return createdOn.toInstant();
    }

    /**
     * If {@link #getExpiresOn()} is null, then this will return null. Otherwise
     * it will return the number of seconds before expiration.
     *
     * @return
     */
    public Integer getExpiresInSeconds() {

        if (expiresOn == null) {
            return null;
        }
        return (int) Duration.between(Instant.now(), expiresOn.toInstant()).getSeconds();
    }

    public Instant getExpiresOn() {

        if (expiresOn == null) {
            return null;
        }
        return expiresOn.toInstant();
    }

    public String getJwt() {

        return jwt;
    }

    public String getRefreshToken() {

        return refreshToken;
    }

    /**
     * If {@link #getExpiresOn()} is null, then this will return false.
     *
     * @return expired
     */
    public boolean isExpired() {

        if (expiresOn == null) {
            return false;
        }
        return Instant.now().isAfter(expiresOn.toInstant());
    }
}
