package com.learning.mastery.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

/**
 * CacheConfig — Redis cache manager with per-cache TTL configuration.
 *
 * Demonstrates Kotlin-idiomatic Spring @Configuration:
 * - @Bean functions return concrete types directly (no wrapping in ResponseEntity, etc.)
 * - Function bodies replace verbose builder chains with Kotlin's apply {} or let {}
 *
 * All cached objects must be JSON-serializable. Use jackson-module-kotlin (declared in
 * build.gradle.kts) so data classes serialize correctly without @JsonProperty annotations.
 *
 * TTL strategy for a retail system:
 * - "products"        → 1 hour   (product data changes rarely)
 * - "product-catalog" → 5 min    (catalog page updated more often)
 * - "user-sessions"   → 24 hours (session persistence)
 * - "promotions"      → 1 min    (flash sales, time-sensitive pricing)
 */
@Configuration  // @EnableCaching lives on MasteryApplication — one declaration is enough
class CacheConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val jsonSerializer = GenericJackson2JsonRedisSerializer()

        val defaults = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer),
            )
            .disableCachingNullValues()

        val cacheConfigs = mapOf(
            "products"        to defaults.entryTtl(Duration.ofHours(1)),
            "product-catalog" to defaults.entryTtl(Duration.ofMinutes(5)),
            "user-sessions"   to defaults.entryTtl(Duration.ofHours(24)),
            "promotions"      to defaults.entryTtl(Duration.ofMinutes(1)),
        )

        log.info("[CacheConfig] Redis cache configured with {} named caches + 30min default TTL", cacheConfigs.size)

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaults)
            .withInitialCacheConfigurations(cacheConfigs)
            .build()
    }
}
