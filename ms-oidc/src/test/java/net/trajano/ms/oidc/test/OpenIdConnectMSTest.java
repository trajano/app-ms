package net.trajano.ms.oidc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.jose4j.jwt.JwtClaims;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.JsonObject;

import net.trajano.ms.oidc.OpenIdConnect;
import net.trajano.ms.oidc.OpenIdConnectResource;
import net.trajano.ms.oidc.internal.AuthenticationUriBuilder;
import net.trajano.ms.vertx.VertxConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    VertxConfig.class,
    ConcurrentMapCacheManager.class,
    AuthenticationUriBuilder.class,
    OpenIdConnect.class,
    OpenIdConnectResource.class,
    TestConfig.class
})
@TestPropertySource(properties = {
    "authorization.endpoint=http://example.trajano.net",
})
public class OpenIdConnectMSTest {

    @Autowired
    private AuthenticationUriBuilder builder;

    @Autowired
    private OpenIdConnect ms;

    @Autowired
    private OpenIdConnectResource resource;

    @Test
    public void builderTest() {

        builder.build("state", "issuer", "auhtoriation", new JwtClaims());
    }

    @Test
    public void exampleTest() {

        assertNotNull(ms);
    }

    @Test
    public void testOidcResource() {

        Assert.assertNotNull(resource);
        final Response auth = resource.auth("abc", "issuer", "jdk");
        assertEquals("example.trajano.net", URI.create(auth.getHeaderString("Location")).getHost());
        final JsonObject authUriJson = resource.authUriJson("abc", "issuer", "jdk");
        assertEquals("example.trajano.net", URI.create(authUriJson.get("uri").getAsString()).getHost());

    }

}
