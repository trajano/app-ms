package net.trajano.ms.oidc.internal;

import java.io.File;
import java.io.IOException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Configuration;

import net.trajano.ms.core.JsonOps;
import net.trajano.ms.oidc.OpenIdConfiguration;

@Configuration
public class ServiceConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceConfiguration.class);

    @Autowired(required = false)
    private CacheManager cacheManager;

    private Map<String, IssuerConfig> issuers;

    @Value("${issuersJson:openidconnect-config.json}")
    private String issuersJson;

    @Inject
    private JsonOps jsonOps;

    @Value("${oidc_config_file:openidconnect-config.json}")
    private File oidcFile;

    @Value("${redirect_uri}")
    private URI redirectUri;

    @Value("${token_endpoint:}")
    private URI tokenEndpoint;

    public IssuerConfig getIssuerConfig(final String issuerId) {

        return issuers.get(issuerId);
    }

    public URI getRedirectUri() {

        return redirectUri;
    }

    @PostConstruct
    public void init() throws IOException {

        final Client client = ClientBuilder.newClient();

        final IssuersConfig issuersConfig = jsonOps.fromJson(oidcFile, IssuersConfig.class);

        issuersConfig.getIssuers().forEach(issuer -> {
            issuer.setOpenIdConfiguration(client.target(UriBuilder.fromUri(issuer.getUri()).path("/.well-known/openid-configuration")).request(MediaType.APPLICATION_JSON).get(OpenIdConfiguration.class));
            LOG.info("Registered {} to {}", issuer.getId(), issuer.getUri());
        });
        issuers = issuersConfig.getIssuers().stream()
            .collect(Collectors.toMap(IssuerConfig::getId, Function.identity()));

        if (cacheManager == null) {
            cacheManager = new ConcurrentMapCacheManager();
        }

    }

}
