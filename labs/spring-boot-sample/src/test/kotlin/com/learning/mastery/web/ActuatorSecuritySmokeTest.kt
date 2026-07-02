package com.learning.mastery.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(
    properties = [
        "spring.cache.type=simple",
        "management.health.redis.enabled=false",
    ],
)
@AutoConfigureMockMvc
class ActuatorSecuritySmokeTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `health endpoint stays public`() {
        mockMvc.get("/actuator/health").andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `metrics endpoint is rejected for anonymous caller`() {
        mockMvc.get("/actuator/metrics").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithMockUser(roles = ["OPS"])
    fun `ops caller can access metrics endpoint`() {
        mockMvc.get("/actuator/metrics").andExpect {
            status { isOk() }
        }
    }
}
