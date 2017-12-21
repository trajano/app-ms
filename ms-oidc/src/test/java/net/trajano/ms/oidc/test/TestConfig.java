package net.trajano.ms.oidc.test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.authz.spi.ClientValidator;
import net.trajano.ms.oidc.OpenIdConfiguration;
import net.trajano.ms.oidc.spi.IssuerConfig;
import net.trajano.ms.oidc.spi.ServiceConfiguration;

@Configuration
public class TestConfig {

    @Bean
    public ClientValidator clientValidator() throws Exception {

        final ClientValidator mock = mock(ClientValidator.class);
        when(mock.isValid(eq(GrantTypes.OPENID), anyString())).thenReturn(true);
        return mock;
    }

    @Bean
    public ServiceConfiguration serviceConfiguration() {

        final ServiceConfiguration mock = mock(ServiceConfiguration.class);

        final OpenIdConfiguration openIdConfiguration = new OpenIdConfiguration();
        openIdConfiguration.setAuthorizationEndpoint(URI.create("http://example.trajano.net"));

        final IssuerConfig issuerConfig = new IssuerConfig();
        issuerConfig.setOpenIdConfiguration(openIdConfiguration);
        issuerConfig.setScope("openid");
        issuerConfig.setClientId("clientid");
        issuerConfig.setClientSecret("clientsecret");
        when(mock.getIssuerConfig("issuer")).thenReturn(issuerConfig);
        when(mock.getRedirectUri()).thenReturn(URI.create("http://example.trajano.net"));
        return mock;
    }
}
