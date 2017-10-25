package net.trajano.ms.oidc.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.context.junit4.SpringRunner;

import net.trajano.ms.engine.internal.spring.SpringConfiguration;
import net.trajano.ms.engine.jaxrs.CommonObjectMapperProvider;
import net.trajano.ms.oidc.OpenIdConnect;
import net.trajano.ms.vertx.beans.GsonJacksonJsonOps;
import net.trajano.ms.vertx.beans.GsonProvider;
import net.trajano.ms.vertx.beans.JcaCryptoOps;
import net.trajano.ms.vertx.beans.JwksProvider;
import net.trajano.ms.vertx.beans.TokenGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    GsonJacksonJsonOps.class,
    GsonProvider.class,
    JcaCryptoOps.class,
    JwksProvider.class,
    TokenGenerator.class,
    CommonObjectMapperProvider.class,
    SpringConfiguration.class,
    ConcurrentMapCacheManager.class,
    OpenIdConnect.class
})

public class OpenIdConnectMSTest {

    @Autowired
    private OpenIdConnect ms;

    @Test
    public void exampleTest() {

        Assert.assertNotNull(ms);
    }

}
