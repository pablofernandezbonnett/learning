package com.learning.mastery.web

import com.learning.mastery.web.exceptions.ProductNotFoundException
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.Instant

/**
 * ProductControllerTest — @WebMvcTest slice test for ProductController.
 *
 * @WebMvcTest:
 * - Loads only the web layer (controllers, filters, advice) — NOT the full context.
 * - Much faster than @SpringBootTest.
 * - @MockBean replaces the real ProductService with a Mockito mock.
 *
 * Kotlin test idioms:
 * - MockMvc DSL (test { ... }) is cleaner than Java's fluent API chains.
 * - whenever() replaces Mockito.when() (avoids conflict with Kotlin's 'when' keyword).
 *
 * Note: spring-boot-starter-test includes mockito-kotlin via the
 *   org.mockito.kotlin:mockito-kotlin transitive dependency.
 */
@WebMvcTest(ProductController::class)
class ProductControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var productService: ProductService

    // ── GET /api/products/{id} ────────────────────────────────────────────────

    @Test
    fun `getById returns 200 with product when found`() {
        val product = ProductResponse(
            id = "J0001",
            name = "Fleece Jacket",
            category = "outerwear",
            priceYen = BigDecimal("5990"),
            stock = 42,
            updatedAt = Instant.now(),
        )
        whenever(productService.findById("J0001")).thenReturn(product)

        mockMvc.get("/api/products/J0001") {
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value("J0001") }
            jsonPath("$.name") { value("Fleece Jacket") }
            jsonPath("$.category") { value("outerwear") }
        }
    }

    @Test
    fun `getById returns 404 ProblemDetail when product not found`() {
        whenever(productService.findById("J9999")).thenThrow(ProductNotFoundException("J9999"))

        mockMvc.get("/api/products/J9999") {
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.title") { value("Product Not Found") }
            jsonPath("$.status") { value(404) }
            jsonPath("$.productId") { value("J9999") }
        }
    }

    // ── GET /api/products ─────────────────────────────────────────────────────

    @Test
    fun `list returns 200 with empty page`() {
        whenever(
            productService.findAll(
                category = null,
                maxPrice = null,
                pageable = org.mockito.kotlin.any<Pageable>(),
            ),
        ).thenReturn(PageImpl(emptyList()))

        mockMvc.get("/api/products").andExpect {
            status { isOk() }
            jsonPath("$.content") { isArray() }
        }
    }

    // ── POST /api/products ────────────────────────────────────────────────────

    @Test
    fun `create returns 201 with Location header`() {
        val created = ProductResponse(
            id = "T0001",
            name = "Heattech Tee",
            category = "tops",
            priceYen = BigDecimal("1990"),
            stock = 100,
            updatedAt = Instant.now(),
        )
        whenever(productService.create(org.mockito.kotlin.any())).thenReturn(created)

        mockMvc.post("/api/products") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "name": "Heattech Tee",
                  "category": "tops",
                  "priceYen": 1990,
                  "initialStock": 100
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            header { string("Location", "/api/products/T0001") }
            jsonPath("$.id") { value("T0001") }
        }
    }

    @Test
    fun `create returns 422 when request body is invalid`() {
        mockMvc.post("/api/products") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "name": "",
                  "category": "invalid-category",
                  "priceYen": -100,
                  "initialStock": -1
                }
            """.trimIndent()
        }.andExpect {
            status { isUnprocessableEntity() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.title") { value("Validation Failed") }
            jsonPath("$.errors") { isMap() }
        }
    }
}
