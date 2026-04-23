package com.learning.mastery

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync

/**
 * Entry point for the Spring Boot Mastery learning lab.
 *
 * Annotations on this class activate framework features globally:
 * - @SpringBootApplication  = @Configuration + @EnableAutoConfiguration + @ComponentScan
 * - @EnableCaching          = activates Spring's cache proxy (@Cacheable, @CacheEvict, ...)
 * - @EnableJpaAuditing      = auto-populates @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy
 * - @EnableAsync            = activates @Async support (runs methods on a separate executor thread)
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableAsync
class MasteryApplication

fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger(MasteryApplication::class.java)
    log.info("[MasteryApplication] Starting Spring Boot Mastery lab...")
    runApplication<MasteryApplication>(*args)
    log.info("[MasteryApplication] Application started successfully")
}
