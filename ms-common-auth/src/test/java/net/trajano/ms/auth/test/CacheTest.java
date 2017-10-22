package net.trajano.ms.auth.test;

import net.trajano.ms.authz.internal.CacheNames;
import net.trajano.ms.authz.internal.LoggingEntryListener;
import net.trajano.ms.authz.internal.TokenCache;
import net.trajano.ms.vertx.beans.JcaCryptoOps;
import net.trajano.ms.vertx.beans.JwksProvider;
import net.trajano.ms.vertx.beans.TokenGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SampleInternalClaimsBuilder.class,
    JwksProvider.class,
    LoggingEntryListener.class,
    ConcurrentMapCacheManager.class,
    TokenCache.class,
    JcaCryptoOps.class,
    TokenGenerator.class
})
@EnableCaching
@CacheConfig(cacheNames = {
    CacheNames.ACCESS_TOKEN_TO_ENTRY,
    CacheNames.REFRESH_TOKEN_TO_ENTRY
})
public class CacheTest {

    @Autowired
    private TokenCache tokenCache;

    @Test
    public void testCache() {

        assertNotNull(tokenCache);
        assertNull(tokenCache.get("abc"));
    }

}
