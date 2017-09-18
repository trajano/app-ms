package net.trajano.ms.oidc.test;

import java.net.URI;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import net.trajano.ms.oidc.OpenIdConfiguration;
import net.trajano.ms.oidc.internal.IssuerConfig;

public class SimpleClientTest {

    @Test
    public void testWithResource() throws Exception {

        final IssuerConfig issuerConfig = new IssuerConfig();
        issuerConfig.setUri(URI.create("https://accounts.google.com"));
        //        final OpenIdConfiguration openIdConfiguration = new MSF4JClient.Builder<WellKnownAPI>().apiClass(WellKnownAPI.class).serviceEndpoint(issuerConfig.getUri().toASCIIString()).build().api().openIdConfiguration();
        final OpenIdConfiguration openIdConfiguration = ClientBuilder.newClient()
            .register(JacksonJaxbJsonProvider.class)
            .target(UriBuilder.fromUri(issuerConfig.getUri()).path("/.well-known/openid-configuration")).request(MediaType.APPLICATION_JSON).get(OpenIdConfiguration.class);
        System.out.println(openIdConfiguration);
        Assert.assertNotNull(openIdConfiguration.getAuthorizationEndpoint());
        issuerConfig.setOpenIdConfiguration(openIdConfiguration);
        //            new MSF4JClient.Builder<WellKnownAPI>().apiClass(WellKnownAPI.class).serviceEndpoint(issuerConfig.getUri().toASCIIString()).build().api().openIdConfiguration());
        issuerConfig.buildAuthenticationRequestUri("abcde");
    }

}
