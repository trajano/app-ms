package net.trajano.ms.auth.internal;

import com.hazelcast.config.Config;
import com.hazelcast.config.EntryListenerConfig;
import com.hazelcast.config.MapConfig;
import net.trajano.ms.core.Qualifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static net.trajano.ms.auth.internal.CacheNames.JWKS;

@Configuration
public class HazelcastConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastConfiguration.class);

    @Value("${token.access_token_expiration:300}")
    private int accessTokenExpirationInSeconds;

    @Autowired
    private LoggingEntryListener entryListener;

    @Value("${cache.instance_name:authz}")
    private String instanceName;

    @Value("${token.refresh_token_expiration:3600}")
    private int refreshTokenExpirationInSeconds;

    @Value("${token.jwk_expiration:1800}")
    private int jwkExpirationInSeconds;

    @Bean
    public Config hazelcastConfig() {

        final EntryListenerConfig listener = new EntryListenerConfig();
        if (LOG.isDebugEnabled()) {
            listener.setLocal(true)
                .setImplementation(entryListener);
        }
        final Config config = new Config()
            .setInstanceName(instanceName)
            .setProperty("hazelcast.logging.type", "slf4j")
            .addMapConfig(new MapConfig()
                .setName(CacheNames.ACCESS_TOKEN_TO_ENTRY)
                .setTimeToLiveSeconds(accessTokenExpirationInSeconds)
                .setMaxIdleSeconds(accessTokenExpirationInSeconds)
                .addEntryListenerConfig(listener))
            .addMapConfig(new MapConfig()
                .setName(CacheNames.REFRESH_TOKEN_TO_ENTRY)
                .setTimeToLiveSeconds(refreshTokenExpirationInSeconds)
                .setMaxIdleSeconds(refreshTokenExpirationInSeconds)
                .addEntryListenerConfig(listener))
            .addMapConfig(new MapConfig()
                .setName(JWKS)
                .setTimeToLiveSeconds(jwkExpirationInSeconds)
                .setMaxIdleSeconds(jwkExpirationInSeconds)
                .addEntryListenerConfig(listener));

        LOG.debug("hazelcast config={}", config);
        return config;

    }

    @Bean(Qualifiers.JWKS_CACHE)
    public Cache jwksCache(CacheManager cm) {

        return cm.getCache(JWKS);
    }

}
