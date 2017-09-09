package net.trajano.ms.common;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JwksProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JwksProvider.class);

    /**
     * Maximum number of keys to keep in the cache.
     */
    private static final int MAX_NUMBER_OF_KEYS = 5;

    private static final int MIN_NUMBER_OF_KEYS = 2;

    /**
     * This is a cache of JWKs.
     */
    @Autowired
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
    @PostConstruct
    @Scheduled(fixedDelay = 60000)
    public void buildJwks() throws JoseException {

        int nCreated = 0;
        for (int i = 0; i < MAX_NUMBER_OF_KEYS; ++i) {
            final String cacheKey = String.valueOf(i);
            RsaJsonWebKey jwk = jwksCache.get(cacheKey, RsaJsonWebKey.class);
            if (jwk == null && nCreated < MIN_NUMBER_OF_KEYS) {
                jwk = RsaJwkGenerator.generateJwk(2048, null, tokenGenerator.random());
                jwk.setKeyId(tokenGenerator.newToken());
                ++nCreated;
                LOG.debug("Created new JWK kid={0}", jwk.getKeyId());
            }
        }

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

}
