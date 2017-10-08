package net.trajano.ms.example.authz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;

@Configuration
public class HazelcastConfiguration {

    @Value("${token.accessTokenExpiration:300}")
    private int accessTokenExpirationInSeconds;

    @Value("${cache.instanceName:authz}")
    private String instanceName;

    @Value("${token.refreshTokenExpiration:3600}")
    private int refreshTokenExpirationInSeconds;

    @Bean
    public Config hazelcastConfig() {

        return new Config()
            .setInstanceName(instanceName)
            .addMapConfig(new MapConfig()
                .setName(TokenCache.ACCESS_TOKEN_TO_CLAIMS)
                .setTimeToLiveSeconds(accessTokenExpirationInSeconds))
            .addMapConfig(new MapConfig()
                .setName(TokenCache.REFRESH_TOKEN_TO_ACCESS_TOKEN)
                .setTimeToLiveSeconds(refreshTokenExpirationInSeconds))
            .addMapConfig(new MapConfig()
                .setName(TokenCache.REFRESH_TOKEN_TO_CLAIMS)
                .setTimeToLiveSeconds(refreshTokenExpirationInSeconds));

    }

}
