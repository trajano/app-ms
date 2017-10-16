package net.trajano.ms.example.authz;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfiguration {

    @Value("${token.accessTokenExpiration:300}")
    private int accessTokenExpirationInSeconds;

    @Value("${cache.instanceName:authz}")
    private String instanceName;

    @Value("${token.refreshTokenExpiration:3600}")
    private int refreshTokenExpirationInSeconds;

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastConfiguration.class);
    //
    //    @Bean
    //    public HazelcastInstance cacheManager(Config config) {
    //        return Hazelcast.newHazelcastInstance(config);
    //    }

    @Bean
    public Config hazelcastConfig() {

        Config config = new Config()
            .setInstanceName(instanceName)
            .setProperty("hazelcast.logging.type", "slf4j")
            .addMapConfig(new MapConfig()
                .setName(TokenCache.ACCESS_TOKEN_TO_CLAIMS)
                .setTimeToLiveSeconds(accessTokenExpirationInSeconds)
                .setMaxIdleSeconds(accessTokenExpirationInSeconds))
            .addMapConfig(new MapConfig()
                .setName(TokenCache.REFRESH_TOKEN_TO_ACCESS_TOKEN)
                .setTimeToLiveSeconds(refreshTokenExpirationInSeconds)
                .setMaxIdleSeconds(refreshTokenExpirationInSeconds))
            .addMapConfig(new MapConfig()
                .setName(TokenCache.REFRESH_TOKEN_TO_CLAIMS)
                .setTimeToLiveSeconds(refreshTokenExpirationInSeconds)
                .setMaxIdleSeconds(refreshTokenExpirationInSeconds));
        LOG.debug("hazelcast config={}", config);
        return config;
        //                .setListenerConfigs(Arrays.asList(new ListenerConfig()
        //                .setClassName(EntryChangeListener.class.getName())));

    }

}
