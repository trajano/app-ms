package net.trajano.ms.vertx.test;

import static org.junit.Assert.assertNotNull;

import net.trajano.ms.vertx.beans.CachedDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.trajano.ms.core.CryptoOps;
import net.trajano.ms.vertx.beans.JcaCryptoOps;
import net.trajano.ms.vertx.beans.TokenGenerator;

/**
 * Tests are hanging on Travis for some odd reason.
 *
 * @author Archimedes Trajano
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    JcaCryptoOps.class,
    CachedDataProvider.class,
    TokenGenerator.class
})
public class OpsTest {

    @Autowired
    private CryptoOps cryptoOps;

    @Test
    public void testNotNull() throws Exception {

        assertNotNull(cryptoOps);
        assertNotNull(cryptoOps.newToken());
    }

}
