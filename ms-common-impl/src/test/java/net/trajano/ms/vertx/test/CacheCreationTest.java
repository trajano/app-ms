package net.trajano.ms.vertx.test;

import net.trajano.ms.sample.JwksResource;
import net.trajano.ms.vertx.beans.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests are hanging on Travis for some odd reason.
 *
 * @author Archimedes Trajano
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    JwtClaimsProcessor.class,
    JcaCryptoOps.class,
    TokenGenerator.class,
    JwksProvider.class
})
public class CacheCreationTest {

    @Autowired
    private JwksProvider jwksProvider;

    /**
     * Verify that the concurrent cache manager returns something.
     */
    @Test
    public void springConcurrentMapCacheManagerTest() throws Exception {

        assertNotNull(jwksProvider);

    }

    @Test
    public void testJwksResource() throws Exception {

        final JwksResource jwksResource = new JwksResource();
        jwksResource.setJwksProvider(jwksProvider);
        System.out.println(jwksResource.getPublicKeySet());
        //        @SuppressWarnings("unchecked")
        //        final List<JsonWebKey> keys = (List<JsonWebKey>) jwksResource.getPublicKeySet().getEntity();
        //        assertEquals(JwksProvider.MIN_NUMBER_OF_KEYS, keys.size());
    }

    @Test
    public void testProvider() throws Exception {

        assertNotNull(jwksProvider.getKeySet());
        assertEquals(JwksProvider.MIN_NUMBER_OF_KEYS, jwksProvider.getKeySet().getJsonWebKeys().size());
    }

}
