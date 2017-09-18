package net.trajano.ms.oidc.test;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Test;

import net.trajano.ms.common.JaxRsClientProvider;
import net.trajano.ms.oidc.OpenIdConfiguration;
import net.trajano.ms.oidc.internal.IssuerConfig;

public class SimpleClientTest {

    @Test
    public void testWithResource() throws Exception {

        final IssuerConfig issuerConfig = new IssuerConfig();
        issuerConfig.setUri(URI.create("https://accounts.google.com"));
        issuerConfig.setScope("openid");
        issuerConfig.setClientId("asdf");
        final JaxRsClientProvider jaxRsClientProvider = new JaxRsClientProvider();
        //        final OpenIdConfiguration openIdConfiguration = new MSF4JClient.Builder<WellKnownAPI>().apiClass(WellKnownAPI.class).serviceEndpoint(issuerConfig.getUri().toASCIIString()).build().api().openIdConfiguration();
        final OpenIdConfiguration openIdConfiguration = jaxRsClientProvider.clientBuilder().build()
            .target(UriBuilder.fromUri(issuerConfig.getUri()).path("/.well-known/openid-configuration")).request(MediaType.APPLICATION_JSON).get(OpenIdConfiguration.class);
        System.out.println(openIdConfiguration);
        Assert.assertNotNull(openIdConfiguration.getAuthorizationEndpoint());
        issuerConfig.setOpenIdConfiguration(openIdConfiguration);
        //            new MSF4JClient.Builder<WellKnownAPI>().apiClass(WellKnownAPI.class).serviceEndpoint(issuerConfig.getUri().toASCIIString()).build().api().openIdConfiguration());
        issuerConfig.buildAuthenticationRequestUri(URI.create("https://localhost/cb"), "abcde", "nonce");
    }

}
