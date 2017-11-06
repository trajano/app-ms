package net.trajano.ms.oidc.test;

import net.trajano.ms.auth.spi.ClientValidator;
import net.trajano.ms.auth.token.GrantTypes;
import net.trajano.ms.oidc.spi.IssuerConfig;
import net.trajano.ms.oidc.spi.ServiceConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

import static org.mockito.Mockito.*;

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
        when(mock.getIssuerConfig("issuer")).thenReturn(mock(IssuerConfig.class));
        when(mock.getRedirectUri()).thenReturn(URI.create("http://example.trajano.net"));
        return mock;
    }
}
