package net.trajano.ms.common.internal;

import javax.annotation.PostConstruct;

import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.keys.resolvers.DecryptionKeyResolver;
import org.jose4j.keys.resolvers.JwksDecryptionKeyResolver;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.trajano.ms.common.TokenGenerator;

@Component
public class JwksProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JwksProvider.class);

    /**
     * Maximum number of keys to keep in the cache.
     */
    public static final int MAX_NUMBER_OF_KEYS = 5;

    public static final int MIN_NUMBER_OF_KEYS = 2;

    /**
     * This is a cache of JWKs. If this is not provided a default one is used.
     */
    @Autowired(required = false)
    @Qualifier("jwks_cache")
    private Cache jwksCache;

    @Autowired
    private TokenGenerator tokenGenerator;

    /**
     * Builds JWKS if necessary after 60 seconds, but only builds
     * {@value #MIN_NUMBER_OF_KEYS} at a time.
     *
     * @throws JoseException
     */
    @Scheduled(fixedDelay = 60000)
    public void buildJwks() throws JoseException {

        int nCreated = 0;
        for (int i = 0; i < MAX_NUMBER_OF_KEYS; ++i) {
            final String cacheKey = String.valueOf(i);
            RsaJsonWebKey jwk = jwksCache.get(cacheKey, RsaJsonWebKey.class);
            if (jwk == null && nCreated < MIN_NUMBER_OF_KEYS) {
                jwk = RsaJwkGenerator.generateJwk(2048, null, tokenGenerator.random());
                jwk.setKeyId(tokenGenerator.newToken());
                jwksCache.putIfAbsent(cacheKey, jwk);
                ++nCreated;
                LOG.debug("Created new JWK kid={0}", jwk.getKeyId());
            }
        }

    }

    @PostConstruct
    public void checkCache() throws JoseException {

        if (jwksCache == null) {
            LOG.warn("A org.springframework.cache.Cache named 'jwks_cache' was not provided an in-memory cache will be used");
            final ConcurrentMapCacheManager cm = new ConcurrentMapCacheManager("jwks_cache");
            jwksCache = cm.getCache("jwks_cache");
        }
        LOG.debug("cache=" + jwksCache);
        buildJwks();
    }

    public DecryptionKeyResolver getDecryptionKeyResolver() {

        return new JwksDecryptionKeyResolver(getKeySet().getJsonWebKeys());
    }

    /**
     * Builds the keys from the cache.
     *
     * @return
     */
    public JsonWebKeySet getKeySet() {

        final JsonWebKeySet keySet = new JsonWebKeySet();
        for (int i = 0; i < MAX_NUMBER_OF_KEYS; ++i) {
            final String cacheKey = String.valueOf(i);
            final RsaJsonWebKey jwk = jwksCache.get(cacheKey, RsaJsonWebKey.class);
            if (jwk != null) {
                keySet.addJsonWebKey(jwk);
            }
        }
        return keySet;
    }

    public void setTokenGenerator(final TokenGenerator tokenGenerator) {

        this.tokenGenerator = tokenGenerator;
    }

}
