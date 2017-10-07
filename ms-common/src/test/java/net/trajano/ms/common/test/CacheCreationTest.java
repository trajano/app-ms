package net.trajano.ms.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.security.SecureRandom;

import org.junit.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.nimbusds.jose.jwk.JWKSet;

import net.trajano.ms.common.beans.CryptoProvider;
import net.trajano.ms.common.beans.JwksProvider;
import net.trajano.ms.common.beans.TokenGenerator;
import net.trajano.ms.sample.JwksResource;

public class CacheCreationTest {

    private final CryptoProvider cryptoProvider = new CryptoProvider();

    /**
     * Verify that the concurrent cache manager returns something.
     */
    @Test
    public void springConcurrentMapCacheManagerTest() throws Exception {

        final ConcurrentMapCacheManager cm = new ConcurrentMapCacheManager("aaaKs_cache");
        assertNotNull(cm.getCache("aaaKs_cache"));

        final JwksProvider jwksProvider = new JwksProvider();
        final TokenGenerator tokenGenerator = new TokenGenerator();
        tokenGenerator.setRandom(cryptoProvider.secureRandom());
        jwksProvider.setTokenGenerator(tokenGenerator);
        jwksProvider.setJwksCache(cm.getCache("aaaKs_cache"));
        jwksProvider.setKeyPairGenerator(cryptoProvider.keyPairGenerator());
        jwksProvider.setRandom(cryptoProvider.secureRandom());

        jwksProvider.init();

    }

    @Test
    public void testJwksResource() throws Exception {

        final JwksProvider jwksProvider = new JwksProvider();
        final TokenGenerator tokenGenerator = new TokenGenerator();
        final SecureRandom secureRandom = cryptoProvider.secureRandom();

        tokenGenerator.setRandom(secureRandom);
        jwksProvider.setTokenGenerator(tokenGenerator);
        jwksProvider.setKeyPairGenerator(cryptoProvider.keyPairGenerator());
        jwksProvider.setRandom(secureRandom);
        jwksProvider.init();

        final JwksResource jwksResource = new JwksResource();
        jwksResource.setJwksProvider(jwksProvider);
        System.out.println(jwksResource.getPublicKeySet());
        //        @SuppressWarnings("unchecked")
        //        final List<JsonWebKey> keys = (List<JsonWebKey>) jwksResource.getPublicKeySet().getEntity();
        //        assertEquals(JwksProvider.MIN_NUMBER_OF_KEYS, keys.size());
    }

    @Test
    public void testProvider() throws Exception {

        final JwksProvider jwksProvider = new JwksProvider();
        System.out.println(jwksProvider);
        final TokenGenerator tokenGenerator = new TokenGenerator();
        System.out.println(jwksProvider + "1");
        tokenGenerator.setRandom(cryptoProvider.secureRandom());
        System.out.println(jwksProvider + "2");
        jwksProvider.setTokenGenerator(tokenGenerator);
        System.out.println(jwksProvider + "3");
        jwksProvider.setKeyPairGenerator(cryptoProvider.keyPairGenerator());
        System.out.println(jwksProvider + "4");
        jwksProvider.setRandom(cryptoProvider.secureRandom());
        System.out.println(jwksProvider + "5");
        jwksProvider.init();
        System.out.println(jwksProvider + "6");
        final JWKSet keySet = jwksProvider.getKeySet();
        System.out.println(jwksProvider + "7");
        assertNotNull(keySet);
        assertEquals(JwksProvider.MIN_NUMBER_OF_KEYS, keySet.getKeys().size());
    }

}
