package net.trajano.ms.authz.internal;

import java.time.Instant;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import net.trajano.ms.auth.token.IdTokenResponse;
import net.trajano.ms.auth.token.OAuthTokenResponse;
import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.core.ErrorCodes;
import net.trajano.ms.core.ErrorResponses;

@Component
public class TokenCache {

    /**
     * Logger for TokenCache
     */
    private static final Logger LOG = LoggerFactory.getLogger(TokenCache.class);

    @Value("${token.accessTokenExpiration:300}")
    private int accessTokenExpirationInSeconds;

    private Cache accessTokenToEntry;

    @Autowired
    private CacheManager cm;

    @Autowired
    private CryptoOps cryptoOps;

    private Cache refreshTokenToEntry;

    /**
     * Evicts the entry from the caches.
     *
     * @param cacheEntry
     *            cache entry.
     */
    private void evictEntry(final TokenCacheEntry cacheEntry) {

        accessTokenToEntry.evict(cacheEntry.getAccessToken());
        refreshTokenToEntry.evict(cacheEntry.getRefreshToken());
    }

    /**
     * This will return null if a valid entry was not found.
     *
     * @param accessToken
     *            access token
     * @return OAuth 2.0 ID Token Response
     */
    public IdTokenResponse get(final String accessToken) {

        final TokenCacheEntry cacheEntry = accessTokenToEntry.get(accessToken, TokenCacheEntry.class);
        if (cacheEntry == null) {
            LOG.debug("No entry for accessToken={}", accessToken);
            return null;
        }
        if (cacheEntry.isExpired()) {
            evictEntry(cacheEntry);
            LOG.debug("Entry was expired for accessToken={}", accessToken);
            return null;
        }
        return new IdTokenResponse(accessToken, cacheEntry.getJwt(), cacheEntry.getAudiences(), cacheEntry.getExpiresInSeconds());

    }

    @PostConstruct
    public void init() {

        accessTokenToEntry = cm.getCache(CacheNames.ACCESS_TOKEN_TO_ENTRY);
        refreshTokenToEntry = cm.getCache(CacheNames.REFRESH_TOKEN_TO_ENTRY);
        LOG.debug("cache manager={} accessTokenToEntry={} refreshTokenToEntry={}", cm, accessTokenToEntry, refreshTokenToEntry);
    }

    public OAuthTokenResponse refresh(final String refreshToken,
        final String clientId) {

        final TokenCacheEntry cacheEntry = refreshTokenToEntry.get(refreshToken, TokenCacheEntry.class);
        if (cacheEntry == null) {
            throw ErrorResponses.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Token rejected", "Bearer");
        }
        if (cacheEntry.isExpired()) {
            evictEntry(cacheEntry);
            throw ErrorResponses.badRequest(ErrorCodes.INVALID_REQUEST, "JWT has exceeded life time");
        }
        if (!cacheEntry.getAudiences().contains(clientId)) {
            throw ErrorResponses.badRequest(ErrorCodes.INVALID_REQUEST, "Client mismatch");
        }
        return updateEntry(cacheEntry);

    }

    /**
     * Revokes the refresh token and associated access token. This will only throw
     * an error if the token was not associated with the given client ID.
     *
     * @param refreshToken
     *            refreshToken
     * @param clientId
     *            client ID
     */
    public void revokeRefreshToken(final String refreshToken,
        final String clientId) {

        final TokenCacheEntry cacheEntry = refreshTokenToEntry.get(refreshToken, TokenCacheEntry.class);
        if (cacheEntry == null) {
            return;
        }
        if (!cacheEntry.getAudiences().contains(clientId)) {
            throw ErrorResponses.badRequest(ErrorCodes.INVALID_REQUEST, "Client mismatch");
        }
        evictEntry(cacheEntry);

    }

    /**
     * Stores the internal claims set into the cache and returns an OAuth token.
     *
     * @param jwt
     *            JWT to store
     * @param audiences
     *            audience client IDs, this is usually the gateway.
     * @param expiresOn
     *            JWT expiration
     * @return OAuth 2.0 token response with the new tokens.
     */
    public OAuthTokenResponse store(final String jwt,
        final Collection<String> audiences,
        final Instant expiresOn) {

        final String accessToken = cryptoOps.newToken();
        final String refreshToken = cryptoOps.newToken();
        final TokenCacheEntry newCacheEntry = new TokenCacheEntry(accessToken, refreshToken, jwt, audiences, expiresOn);
        accessTokenToEntry.putIfAbsent(accessToken, newCacheEntry);
        refreshTokenToEntry.putIfAbsent(refreshToken, newCacheEntry);

        final OAuthTokenResponse oauthTokenResponse = new OAuthTokenResponse();
        oauthTokenResponse.setAccessToken(accessToken);
        oauthTokenResponse.setTokenType("Bearer");
        oauthTokenResponse.setExpiresIn(accessTokenExpirationInSeconds);
        oauthTokenResponse.setRefreshToken(refreshToken);

        return oauthTokenResponse;
    }

    /**
     * Stores the cache entry into the caches with updated tokens. It will evict the
     * existing entries as well.
     *
     * @param cacheEntry
     *            entry to store
     * @return OAuth 2.0 token response with the new tokens.
     */
    private OAuthTokenResponse updateEntry(final TokenCacheEntry cacheEntry) {

        evictEntry(cacheEntry);
        return store(cacheEntry.getJwt(), cacheEntry.getAudiences(), cacheEntry.getExpiresOn());
    }

}
