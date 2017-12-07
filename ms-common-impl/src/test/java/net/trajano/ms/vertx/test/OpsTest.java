package net.trajano.ms.vertx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.core.ErrorResponse;
import net.trajano.ms.core.JsonOps;
import net.trajano.ms.vertx.beans.CachedDataProvider;
import net.trajano.ms.vertx.beans.GsonJacksonJsonOps;
import net.trajano.ms.vertx.beans.GsonProvider;
import net.trajano.ms.vertx.beans.JcaCryptoOps;
import net.trajano.ms.vertx.beans.TokenGenerator;

/**
 * Tests are hanging on Travis for some odd reason.
 *
 * @author Archimedes Trajano
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    GsonJacksonJsonOps.class,
    GsonProvider.class,
    JcaCryptoOps.class,
    CachedDataProvider.class,
    TokenGenerator.class,
    TestConfig.class
})
public class OpsTest {

    @Autowired
    private CachedDataProvider cachedDataProvider;

    @Autowired
    private CryptoOps cryptoOps;

    @Autowired
    private JsonOps jsonOps;

    @Test
    public void testBinding() {

        final String error = "{\"error\":\"testing\", \"error_description\": \"blah\"}";
        final ErrorResponse response = jsonOps.fromJson(error, ErrorResponse.class);
        assertEquals("testing", response.getError());
    }

    @Test
    public void testCrypto() throws Exception {

        final JwtClaims claims = new JwtClaims();
        claims.setAudience("mememe");
        final String jwt = cryptoOps.sign(claims);
        cachedDataProvider.getKeySet();
        final HttpsJwks jwks = mock(HttpsJwks.class);
        when(jwks.getJsonWebKeys()).thenReturn(cachedDataProvider.getKeySet().getJsonWebKeys());
        cryptoOps.toClaimsSet(jwt, "mememe", jwks);
    }

    @Test
    public void testNotNull() throws Exception {

        assertNotNull(cryptoOps);
        assertNotNull(cryptoOps.newToken());
    }

}
