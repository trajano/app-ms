package net.trajano.ms.oidc.internal;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import net.trajano.ms.common.beans.AssertionNotRequiredFunction;
import net.trajano.ms.common.beans.JwtAssertionRequiredPredicate;
import net.trajano.ms.oidc.OpenIdConfiguration;

@Configuration
public class ServiceConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Context
    private Client client;

    private Map<String, IssuerConfig> issuers;

    @Value("${issuersJson:openidconnect-config.json}")
    private String issuersJson;

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

        Resource resource = applicationContext.getResource("classpath:" + issuersJson);
        if (!resource.exists()) {
            resource = applicationContext.getResource("file:" + issuersJson);
        }

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JaxbAnnotationModule());
        mapper.readValue(resource.getInputStream(), IssuersConfig.class);

        final IssuersConfig issuersConfig = mapper.readValue(resource.getInputStream(), IssuersConfig.class);

        issuersConfig.getIssuers().forEach(issuer -> {
            issuer.setOpenIdConfiguration(client.target(UriBuilder.fromUri(issuer.getUri()).path("/.well-known/openid-configuration")).request(MediaType.APPLICATION_JSON).get(OpenIdConfiguration.class));
        });
        issuers = issuersConfig.getIssuers().stream()
            .collect(Collectors.toMap(IssuerConfig::getId, Function.identity()));

        if (cacheManager == null) {
            cacheManager = new ConcurrentMapCacheManager();
        }

    }

    @Bean
    public JwtAssertionRequiredPredicate noAssertionRequired() {

        return new AssertionNotRequiredFunction();
    }

    @Bean(name = "nonce")
    public Cache nonceCache() {

        return cacheManager.getCache("nonce");
    }

}
