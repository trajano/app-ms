package net.trajano.ms.vertx.beans;

import static net.trajano.ms.core.Qualifiers.JWKS_CACHE;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;

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
    private Cache jwksCache;

    private KeyPairGenerator keyPairGenerator;

    private Random random;

    private TokenGenerator tokenGenerator;

    /**
     * Builds JWKS if necessary after 60 seconds, but only builds
     * {@value #MIN_NUMBER_OF_KEYS} at a time.
     */
    @Scheduled(fixedDelay = 60000)
    public void buildJwks() {

        int nCreated = 0;
        for (int i = 0; i < MAX_NUMBER_OF_KEYS; ++i) {
            final String cacheKey = String.valueOf(i);
            final String jwkJson = jwksCache.get(cacheKey, String.class);
            JWK jwk = null;
            try {
                if (jwkJson != null) {
                    jwk = JWK.parse(jwkJson);
                }
            } catch (final ParseException e) {
                LOG.error("unable to parse key={} json={} recreating entry", cacheKey, jwkJson);
            }
            if (jwk == null && nCreated < MIN_NUMBER_OF_KEYS) {
                jwk = buildNewRsaKey();
                jwksCache.putIfAbsent(cacheKey, jwk.toJSONString());
                ++nCreated;
                LOG.debug("Created new JWK kid={}", jwk.getKeyID());
            }
        }

    }

    private JWK buildNewRsaKey() {

        final KeyPair keyPair = keyPairGenerator.generateKeyPair();

        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
            .privateKey((RSAPrivateKey) keyPair.getPrivate())
            .keyID(tokenGenerator.newToken())
            .build();
    }

    /**
     * Gets a single signing key.
     *
     * @return
     */
    public RSAKey getASigningKey() {

        final List<JWK> keys = getKeySet().getKeys();
        return (RSAKey) keys.get(random.nextInt(keys.size()));
    }

    public RSAKey getDecryptionKey(final String keyID) {

        return (RSAKey) getKeySet().getKeyByKeyId(keyID);

    }

    /**
     * Builds the keys from the cache.
     *
     * @return
     */
    public JWKSet getKeySet() {

        final List<JWK> keys = new LinkedList<>();
        for (int i = 0; i < MAX_NUMBER_OF_KEYS; ++i) {
            final String cacheKey = String.valueOf(i);
            final String jwkJson = jwksCache.get(cacheKey, String.class);
            try {
                if (jwkJson != null) {
                    keys.add(JWK.parse(jwkJson));
                }
            } catch (final ParseException e) {
                LOG.error("unable to parse key={} json={} removing entry", cacheKey, jwkJson);
                jwksCache.evict(cacheKey);
            }
        }
        return new JWKSet(keys);
    }

    @PostConstruct
    public void init() {

        if (jwksCache == null) {
            LOG.warn("A org.springframework.cache.Cache named {} was not provided an in-memory cache will be used", JWKS_CACHE);
            final ConcurrentMapCacheManager cm = new ConcurrentMapCacheManager(JWKS_CACHE);
            jwksCache = cm.getCache(JWKS_CACHE);
        }
        LOG.debug("cache={}", jwksCache);
        buildJwks();
    }

    @Autowired(required = false)
    @Qualifier(JWKS_CACHE)
    public void setJwksCache(final Cache jwksCache) {

        this.jwksCache = jwksCache;
    }

    @Autowired
    public void setKeyPairGenerator(final KeyPairGenerator keyPairGenerator) {

        this.keyPairGenerator = keyPairGenerator;

    }

    @Autowired
    public void setRandom(final Random random) {

        this.random = random;
    }

    @Autowired
    public void setTokenGenerator(final TokenGenerator tokenGenerator) {

        this.tokenGenerator = tokenGenerator;
    }

    /**
     * This will sign a JWT Claims Set and return a JWS object.
     *
     * @param claims
     *            claims to sign
     * @return JWS Object
     * @throws JOSEException
     */
    public JWSObject sign(final JWTClaimsSet claims) throws JOSEException {

        final RSAKey aSigningKey = getASigningKey();

        final JWSObject jws = new JWSObject(
            new JWSHeader.Builder(JWSAlgorithm.RS512)
                .keyID(aSigningKey.getKeyID())
                .build(),
            new Payload(claims.toString()));
        jws.sign(new RSASSASigner(aSigningKey));
        return jws;
    }
}
