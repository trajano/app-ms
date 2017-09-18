package net.trajano.ms.oidc.test;

import java.net.URI;

import org.junit.Test;
import org.wso2.msf4j.client.MSF4JClient;

import net.trajano.ms.oidc.internal.IssuerConfig;
import net.trajano.ms.oidc.internal.WellKnownAPI;

public class SimpleClientTest {

    @Test
    public void testGoogle() throws Exception {

        new MSF4JClient.Builder<WellKnownAPI>().apiClass(WellKnownAPI.class).serviceEndpoint("https://accounts.google.com").build().api().openIdConfiguration();
    }

    @Test
    public void testWithResource() throws Exception {

        final IssuerConfig issuerConfig = new IssuerConfig();
        issuerConfig.setUri(URI.create("https://accounts.google.com"));
        issuerConfig.setOpenIdConfiguration(
            new MSF4JClient.Builder<WellKnownAPI>().apiClass(WellKnownAPI.class).serviceEndpoint(issuerConfig.getUri().toASCIIString()).build().api().openIdConfiguration());
        issuerConfig.buildAuthenticationRequestUri("abcde");
    }

}
