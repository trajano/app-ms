package net.trajano.ms.example.beans;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.trajano.ms.spi.CacheNames;

@Configuration
public class CacheProvider {

    @Bean
    public CacheManager cacheManager() {

        final ConcurrentMapCacheManager concurrentMapCacheManager = new ConcurrentMapCacheManager(CacheNames.JWKS, CacheNames.NONCE);
        return concurrentMapCacheManager;
    }

    @Bean(name = CacheNames.JWKS)
    public Cache jwsCache(final CacheManager cacheManager) {

        return cacheManager.getCache(CacheNames.JWKS);
    }
}
