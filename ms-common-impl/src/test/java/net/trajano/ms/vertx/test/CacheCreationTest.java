package net.trajano.ms.vertx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import net.trajano.ms.vertx.beans.CachedDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.trajano.ms.sample.JwksResource;
import net.trajano.ms.vertx.VertxConfig;

/**
 * Tests are hanging on Travis for some odd reason.
 *
 * @author Archimedes Trajano
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    VertxConfig.class
})
public class CacheCreationTest {

    @Autowired
    private CachedDataProvider cachedDataProvider;

    /**
     * Verify that the concurrent cache manager returns something.
     */
    @Test
    public void springConcurrentMapCacheManagerTest() throws Exception {

        assertNotNull(cachedDataProvider);

    }

    @Test
    public void testJwksResource() throws Exception {

        final JwksResource jwksResource = new JwksResource();
        jwksResource.setCachedDataProvider(cachedDataProvider);
        System.out.println(jwksResource.getPublicKeySet());
        //        @SuppressWarnings("unchecked")
        //        final List<JsonWebKey> keys = (List<JsonWebKey>) jwksResource.getPublicKeySet().getEntity();
        //        assertEquals(CachedDataProvider.MIN_NUMBER_OF_KEYS, keys.size());
    }

    @Test
    public void testProvider() throws Exception {

        assertNotNull(cachedDataProvider.getKeySet());
        assertEquals(CachedDataProvider.MIN_NUMBER_OF_KEYS, cachedDataProvider.getKeySet().getJsonWebKeys().size());
    }

}
