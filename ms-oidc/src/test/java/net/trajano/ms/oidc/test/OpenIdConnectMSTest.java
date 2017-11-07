package net.trajano.ms.oidc.test;

import net.trajano.ms.oidc.OpenIdConnect;
import net.trajano.ms.oidc.internal.AuthenticationUriBuilder;
import net.trajano.ms.vertx.VertxConfig;
import org.jose4j.jwt.JwtClaims;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    VertxConfig.class,
    ConcurrentMapCacheManager.class,
    AuthenticationUriBuilder.class,
    OpenIdConnect.class,
    TestConfig.class
})
public class OpenIdConnectMSTest {

    @Autowired
    private OpenIdConnect ms;

    @Autowired
    private AuthenticationUriBuilder builder;

    @Test
    public void exampleTest() {

        Assert.assertNotNull(ms);
    }

    @Test
    public void builderTest() {

        builder.build("state", "issuer", "auhtoriation", new JwtClaims());
    }

}
