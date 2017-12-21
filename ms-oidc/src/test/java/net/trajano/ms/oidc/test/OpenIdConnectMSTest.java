package net.trajano.ms.oidc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
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
        final Client client = mock(Client.class);
        final WebTarget webTarget = mock(WebTarget.class);
        when(client.target(any(URI.class))).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        final Invocation.Builder builder = mock(Invocation.Builder.class);
        when(webTarget.request(anyString())).thenReturn(builder);
        when(builder.header(anyString(), anyString())).thenReturn(builder);
        when(builder.get(String.class)).thenReturn("http://callback.trajano.net");
        resource.setClient(client);
        final Response auth = resource.auth("abc", "issuer", "jdk");
        assertEquals("example.trajano.net", URI.create(auth.getHeaderString("Location")).getHost());
        final JsonObject authUriJson = resource.authUriJson("abc", "issuer", "jdk");
        assertEquals("example.trajano.net", URI.create(authUriJson.get("uri").getAsString()).getHost());

    }

}
