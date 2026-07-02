package com.learning.mastery.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Small service-boundary baseline for the learning sample:
 * - health probes are public
 * - other actuator endpoints require OPS role
 * - business APIs require authentication
 * - everything else is denied by default
 */
@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/actuator/health",
                    "/actuator/health/liveness",
                    "/actuator/health/readiness",
                ).permitAll()
                auth.requestMatchers("/actuator/**").hasRole("OPS")
                auth.requestMatchers("/api/**").authenticated()
                auth.anyRequest().denyAll()
            }
            .httpBasic(Customizer.withDefaults())

        return http.build()
    }
}
