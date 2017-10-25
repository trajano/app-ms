package net.trajano.ms.oidc.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.EntryListenerConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;

import net.trajano.ms.core.Qualifiers;

@Configuration
public class HazelcastConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastConfiguration.class);

    public static final String SERVER_STATE = "server_state";

    @Value("${cache.instance_name:authz}")
    private String instanceName;

    @Value("${token.jwk_expiration:1800}")
    private int jwkExpirationInSeconds;

    @Value("${oidc.server_state_expiration:3600}")
    private int serverStateExpiration;

    @Bean
    public Config hazelcastConfig() {

        final EntryListenerConfig listener = new EntryListenerConfig();
        final Config config = new Config()
            .setNetworkConfig(new NetworkConfig()
                .setJoin(new JoinConfig()
                    .setMulticastConfig(new MulticastConfig()
                        .setEnabled(true))))
            .setInstanceName(instanceName)
            .setProperty("hazelcast.logging.type", "slf4j")
            .addMapConfig(new MapConfig()
                .setName(SERVER_STATE)
                .setTimeToLiveSeconds(serverStateExpiration)
                .setMaxIdleSeconds(serverStateExpiration))
            .addMapConfig(new MapConfig()
                .setName(Qualifiers.JWKS_CACHE)
                .setTimeToLiveSeconds(jwkExpirationInSeconds)
                .setMaxIdleSeconds(jwkExpirationInSeconds)
                .addEntryListenerConfig(listener));

        LOG.debug("hazelcast config={}", config);
        return config;

    }

}
