package net.trajano.ms.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jose4j.jwk.JsonWebKeySet;
import org.junit.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import net.trajano.ms.common.TokenGenerator;
import net.trajano.ms.common.internal.JwksProvider;
import net.trajano.ms.common.internal.JwksResource;

public class CacheCreationTest {

    /**
     * Verify that the concurrent cache manager returns something.
     */
    @Test
    public void springConcurrentMapCacheManagerTest() {

        final ConcurrentMapCacheManager cm = new ConcurrentMapCacheManager("jwKs_cache");
        assertNotNull(cm.getCache("jwKs_cache"));

    }

    @Test
    public void testProvider() throws Exception {

        final JwksProvider jwksProvider = new JwksProvider();
        final TokenGenerator tokenGenerator = new TokenGenerator();
        tokenGenerator.init();
        jwksProvider.setTokenGenerator(tokenGenerator);
        jwksProvider.checkCache();
        final JsonWebKeySet keySet = jwksProvider.getKeySet();
        assertNotNull(keySet);
        assertEquals(JwksProvider.MIN_NUMBER_OF_KEYS, keySet.getJsonWebKeys().size());
    }

    @Test
    public void testResource() throws Exception {

        final JwksProvider jwksProvider = new JwksProvider();
        final TokenGenerator tokenGenerator = new TokenGenerator();
        tokenGenerator.init();
        jwksProvider.setTokenGenerator(tokenGenerator);
        jwksProvider.checkCache();

        final JwksResource jwksResource = new JwksResource();
        jwksResource.setJwksProvider(jwksProvider);
        System.out.println(jwksResource.getPublicKeySet().getEntity());
        //        @SuppressWarnings("unchecked")
        //        final List<JsonWebKey> keys = (List<JsonWebKey>) jwksResource.getPublicKeySet().getEntity();
        //        assertEquals(JwksProvider.MIN_NUMBER_OF_KEYS, keys.size());
    }

}
