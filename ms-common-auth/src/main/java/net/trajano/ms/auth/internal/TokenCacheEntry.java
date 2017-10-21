package net.trajano.ms.auth.internal;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

/**
 * Entries stored inside the cache. The JWT is stored instead of a JwtClaims to
 * allow encrypted JWTs to be used. Only some meta data needs to be exposed.
 */
public class TokenCacheEntry implements
    Serializable {

    private static final long serialVersionUID = -1643672416756418254L;

    private final String accessToken;

    private final String refreshToken;

    private final String jwt;

    private final String clientId;

    private final Instant createdOn;

    private final Instant expiresOn;

    public TokenCacheEntry(String accessToken,
        String refreshToken,
        String jwt,
        String clientId) {
        this(accessToken, refreshToken, jwt, clientId, null);
    }

    public TokenCacheEntry(String accessToken,
        String refreshToken,
        String jwt,
        String clientId,
        Instant expiresOn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.jwt = jwt;
        this.clientId = clientId;
        this.expiresOn = expiresOn;
        this.createdOn = Instant.now();
    }

    public String getClientId() {

        return clientId;
    }

    public String getAccessToken() {

        return accessToken;
    }

    public String getRefreshToken() {

        return refreshToken;
    }

    public String getJwt() {

        return jwt;
    }

    public Instant getCreatedOn() {

        return createdOn;
    }

    public Instant getExpiresOn() {

        return expiresOn;
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
        return Instant.now().isAfter(expiresOn);
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
        return (int) Duration.between(Instant.now(), expiresOn).getSeconds();
    }
}
