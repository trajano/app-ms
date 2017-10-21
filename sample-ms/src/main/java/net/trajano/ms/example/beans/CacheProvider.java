package net.trajano.ms.example.beans;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.trajano.ms.core.Qualifiers;

@Configuration
public class CacheProvider {

    @Bean
    public CacheManager cacheManager() {

        final ConcurrentMapCacheManager concurrentMapCacheManager = new ConcurrentMapCacheManager(Qualifiers.JWKS_CACHE);
        return concurrentMapCacheManager;
    }

    @Bean(name = Qualifiers.JWKS_CACHE)
    public Cache jwsCache(final CacheManager cacheManager) {

        return cacheManager.getCache(Qualifiers.JWKS_CACHE);
    }
}
