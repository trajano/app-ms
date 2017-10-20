package net.trajano.ms.example.authz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.EntryListenerConfig;
import com.hazelcast.config.MapConfig;

@Configuration
public class HazelcastConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastConfiguration.class);

    @Value("${token.accessTokenExpiration:300}")
    private int accessTokenExpirationInSeconds;

    @Autowired
    private LoggingEntryListener entryListener;

    @Value("${cache.instanceName:authz}")
    private String instanceName;

    @Value("${token.refreshTokenExpiration:3600}")
    private int refreshTokenExpirationInSeconds;

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
                .setName(TokenCache.ACCESS_TOKEN_TO_CLAIMS)
                .setTimeToLiveSeconds(accessTokenExpirationInSeconds)
                .setMaxIdleSeconds(accessTokenExpirationInSeconds)
                .addEntryListenerConfig(listener))
            .addMapConfig(new MapConfig()
                .setName(TokenCache.REFRESH_TOKEN_TO_ACCESS_TOKEN)
                .setTimeToLiveSeconds(refreshTokenExpirationInSeconds)
                .setMaxIdleSeconds(refreshTokenExpirationInSeconds)
                .addEntryListenerConfig(listener))
            .addMapConfig(new MapConfig()
                .setName(TokenCache.REFRESH_TOKEN_TO_CLAIMS)
                .setTimeToLiveSeconds(refreshTokenExpirationInSeconds)
                .setMaxIdleSeconds(refreshTokenExpirationInSeconds)
                .addEntryListenerConfig(listener));

        LOG.debug("hazelcast config={}", config);
        return config;

    }

}
