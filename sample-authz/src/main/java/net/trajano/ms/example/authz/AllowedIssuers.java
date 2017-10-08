package net.trajano.ms.example.authz;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("allowedIssuers")
public class AllowedIssuers {

    private Set<String> allowedIssuers;

    public boolean isIssuerAllowed(final String issuer) {

        return allowedIssuers.contains(issuer);
    }
}
