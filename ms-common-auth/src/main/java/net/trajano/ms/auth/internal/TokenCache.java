package net.trajano.ms.auth.internal;

import net.trajano.ms.auth.token.ErrorCodes;
import net.trajano.ms.auth.token.IdTokenResponse;
import net.trajano.ms.common.oauth.OAuthTokenResponse;
import net.trajano.ms.core.CryptoOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;

@Component
public class TokenCache {

    /**
     * Logger for TokenCache
     */
    private static final Logger LOG = LoggerFactory.getLogger(TokenCache.class);

    @Value("${token.accessTokenExpiration:300}")
    private int accessTokenExpirationInSeconds;

    @Autowired
    private CacheManager cm;

    private Cache refreshTokenToEntry;

    private Cache accessTokenToEntry;

    @Autowired
    private CryptoOps cryptoOps;

    /**
     * This will return null if a valid entry was not found.
     * 
     * @param accessToken
     *            access token
     * @param clientId
     *            client ID
     * @return OAuth 2.0 ID Token Response
     */
    public IdTokenResponse get(final String accessToken,
        final String clientId) {

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
        if (!clientId.equals(cacheEntry.getClientId())) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Expected client_id={} for accessToken={} but was {} in the cache", clientId, accessToken, cacheEntry.getClientId());
            }
            return null;
        }
        return new IdTokenResponse(accessToken, cacheEntry.getJwt(), cacheEntry.getExpiresInSeconds());

    }

    /**
     * Evicts the entry from the caches.
     *
     * @param cacheEntry
     *            cache entry.
     */
    private void evictEntry(TokenCacheEntry cacheEntry) {

        accessTokenToEntry.evict(cacheEntry.getAccessToken());
        refreshTokenToEntry.evict(cacheEntry.getRefreshToken());
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
            throw OAuthTokenResponse.unauthorized(ErrorCodes.UNAUTHORIZED_CLIENT, "Token rejected", "Bearer");
        }
        if (cacheEntry.isExpired()) {
            evictEntry(cacheEntry);
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "JWT has exceeded life time");
        }
        if (!clientId.equals(cacheEntry.getClientId())) {
            throw OAuthTokenResponse.badRequest(ErrorCodes.INVALID_REQUEST, "Client mismatch");
        }
        return updateEntry(cacheEntry);

    }

    /**
     * Stores the cache entry into the caches with updated tokens. It will evict
     * the existing entries as well.
     *
     * @param cacheEntry
     *            entry to store
     * @return OAuth 2.0 token response with the new tokens.
     */
    private OAuthTokenResponse updateEntry(final TokenCacheEntry cacheEntry) {

        evictEntry(cacheEntry);
        return store(cacheEntry.getJwt(), cacheEntry.getClientId(), cacheEntry.getExpiresOn());
    }

    /**
     * Stores the internal claims set into the cache and returns an OAuth token.
     *
     * @param jwt
     *            JWT to store
     * @param clientId
     *            client ID
     * @param expiresOn
     *            JWT expiration
     * @return OAuth 2.0 token response with the new tokens.
     */
    public OAuthTokenResponse store(final String jwt,
        final String clientId,
        final Instant expiresOn) {

        final String accessToken = cryptoOps.newToken();
        final String refreshToken = cryptoOps.newToken();
        TokenCacheEntry newCacheEntry = new TokenCacheEntry(accessToken, refreshToken, jwt, clientId, expiresOn);
        accessTokenToEntry.putIfAbsent(accessToken, newCacheEntry);
        refreshTokenToEntry.putIfAbsent(refreshToken, newCacheEntry);

        final OAuthTokenResponse oauthTokenResponse = new OAuthTokenResponse();
        oauthTokenResponse.setAccessToken(accessToken);
        oauthTokenResponse.setTokenType("Bearer");
        oauthTokenResponse.setExpiresIn(accessTokenExpirationInSeconds);
        oauthTokenResponse.setRefreshToken(refreshToken);

        return oauthTokenResponse;
    }

}
