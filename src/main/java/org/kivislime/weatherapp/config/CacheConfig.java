package org.kivislime.weatherapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    private final long cacheDuration;
    private final long cacheSize;

    public CacheConfig(@Value("${cache.weather.expire-minutes}") long cacheDuration,
                       @Value("${cache.weather.max-size}") long cacheSize) {
        this.cacheDuration = cacheDuration;
        this.cacheSize = cacheSize;
    }

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(cacheDuration))
                .maximumSize(cacheSize);
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager manager = new CaffeineCacheManager("weatherByLocation");
        manager.setCaffeine(caffeine);
        return manager;
    }
}
