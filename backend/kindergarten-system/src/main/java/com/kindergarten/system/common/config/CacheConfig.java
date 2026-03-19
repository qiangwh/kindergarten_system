package com.kindergarten.system.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 缓存配置
 * <p>
 * 当前项目首页概览、汇总统计等接口以“读多写少”为主，
 * 适合使用本地缓存减少重复查询数据库的开销。
 * </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 默认使用 Caffeine 本地缓存：容量有限、响应快、无需额外中间件。
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(32)
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofDays(1)));
        return cacheManager;
    }
}
