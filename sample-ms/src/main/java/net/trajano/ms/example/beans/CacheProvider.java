package net.trajano.ms.example.beans;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.trajano.ms.spi.CacheNames;

/**
 * Provides simple caches used by the framework.
 *
 * @author Archimedes Trajano
 */
@Configuration
public class CacheProvider {

    @Bean
    public CacheManager cacheManager() {

        return new ConcurrentMapCacheManager(CacheNames.JWKS, CacheNames.NONCE);
    }

}
