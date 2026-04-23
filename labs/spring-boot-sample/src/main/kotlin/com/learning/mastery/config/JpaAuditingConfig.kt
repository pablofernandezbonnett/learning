package com.learning.mastery.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.Optional

/**
 * JpaAuditingConfig — wires up @CreatedBy / @LastModifiedBy fields.
 *
 * @EnableJpaAuditing activates the auditing infrastructure. The AuditorAware bean
 * provides the "current user" string that Spring writes into @CreatedBy / @LastModifiedBy.
 *
 * In a real application, replace the stub with a call to Spring Security's
 * SecurityContextHolder to get the authenticated username.
 *
 * Note: @EnableJpaAuditing is also declared on MasteryApplication — one declaration
 * is sufficient. Kept here as a separate @Configuration to illustrate the pattern
 * of providing AuditorAware without modifying the main application class.
 */
@Configuration
class JpaAuditingConfig {

    @Bean
    fun auditorProvider(): AuditorAware<String> = AuditorAware {
        // In production: SecurityContextHolder.getContext().authentication?.name
        Optional.of("system")   // stub — always "system" in this learning lab
    }
}
