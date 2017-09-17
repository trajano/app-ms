package net.trajano.ms.oidc.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.trajano.ms.oidc.IssuersConfig;

@Configuration
public class OpenIdConnectConfig {

    @Autowired
    private ApplicationContext applicationContext;

    private IssuersConfig issuers;

    @Value("${issuersJson:openidconnect-config.json}")
    private String issuersJson;

    @Value("${token_endpoint:}")
    private URI tokenEndpoint;

    @Bean
    public IssuersConfig getIssuers() {

        return issuers;
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
        issuers = gson.fromJson(new InputStreamReader(resource.getInputStream()), IssuersConfig.class);
    }

    @Override
    public String toString() {

        return "OpenIdConnectConfig [issuers=" + applicationContext + ", tokenEndpoint=" + tokenEndpoint + "]";
    }

}
