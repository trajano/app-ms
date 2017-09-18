package net.trajano.ms.oidc.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

@Configuration
public class ServiceConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, IssuerConfig> issuers;

    @Value("${issuersJson:openidconnect-config.json}")
    private String issuersJson;

    @Value("${token_endpoint:}")
    private URI tokenEndpoint;

    public IssuerConfig getIssuerConfig(final String issuerId) {

        return issuers.get(issuerId);
    }

    @PostConstruct
    public void loadConfiguration() throws JsonSyntaxException,
        JsonIOException,
        IOException {

        Resource resource = applicationContext.getResource("classpath:" + issuersJson);
        if (!resource.exists()) {
            resource = applicationContext.getResource("file:" + issuersJson);
        }

        final Gson gson = new Gson();
        final IssuersConfig issuersConfig = gson.fromJson(new InputStreamReader(resource.getInputStream()), IssuersConfig.class);
        issuers = issuersConfig.load();

    }

}
