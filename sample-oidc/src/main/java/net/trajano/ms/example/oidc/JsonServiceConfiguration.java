package net.trajano.ms.example.oidc;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import net.trajano.ms.core.JsonOps;
import net.trajano.ms.oidc.OpenIdConfiguration;
import net.trajano.ms.oidc.spi.IssuerConfig;
import net.trajano.ms.oidc.spi.IssuersConfig;
import net.trajano.ms.oidc.spi.ServiceConfiguration;

@Configuration
public class JsonServiceConfiguration implements
    ServiceConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(JsonServiceConfiguration.class);

    private Map<String, IssuerConfig> issuers;

    @Inject
    private JsonOps jsonOps;

    @Value("${oidc.config_file:openidconnect-config.json}")
    private File oidcFile;

    @Value("${oidc.redirect_uri}")
    private URI redirectUri;

    @Value("${token_endpoint:}")
    private URI tokenEndpoint;

    @Override
    public IssuerConfig getIssuerConfig(final String issuerId) {

        return issuers.get(issuerId);
    }

    @Override
    public URI getRedirectUri() {

        return redirectUri;
    }

    @PostConstruct
    public void init() {

        final Client client = ClientBuilder.newClient();

        final IssuersConfig issuersConfig = jsonOps.fromJson(oidcFile, IssuersConfig.class);

        issuersConfig.getIssuers().forEach(issuer -> {
            issuer.setOpenIdConfiguration(client.target(UriBuilder.fromUri(issuer.getUri()).path("/.well-known/openid-configuration")).request(MediaType.APPLICATION_JSON).get(OpenIdConfiguration.class));
            LOG.info("Registered {} to {}", issuer.getId(), issuer.getUri());
        });
        issuers = issuersConfig.getIssuers().stream()
            .collect(Collectors.toMap(IssuerConfig::getId, Function.identity()));

    }

}
