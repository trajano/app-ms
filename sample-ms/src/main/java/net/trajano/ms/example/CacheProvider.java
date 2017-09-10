package net.trajano.ms.example;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheProvider {

    @Bean
    public CacheManager cacheManager() {

        final ConcurrentMapCacheManager concurrentMapCacheManager = new ConcurrentMapCacheManager("jws_cache");
        return concurrentMapCacheManager;
    }

    @Bean(name = "jws_cache")
    public Cache jwsCache(final CacheManager cacheManager) {

        return cacheManager.getCache("jws_cache");
    }
}
