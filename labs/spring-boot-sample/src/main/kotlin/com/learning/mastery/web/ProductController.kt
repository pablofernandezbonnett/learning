package com.learning.mastery.web

import com.learning.mastery.web.exceptions.ProductNotFoundException
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.net.URI
import java.time.Instant

/**
 * ProductController — demonstrates senior-level Spring MVC patterns in Kotlin.
 *
 * Patterns shown:
 * - @Validated on class for @PathVariable / @RequestParam validation
 * - @Valid on @RequestBody for DTO validation
 * - ResponseEntity for precise HTTP control
 * - Data classes as DTOs (replaces Java records)
 * - @field: prefix for validation annotations on data class properties
 * - Pagination with Pageable
 *
 * Kotlin note: plugin.spring auto-opens @RestController so Spring can create CGLIB proxies.
 */
@RestController
@RequestMapping("/api/products")
@Validated   // enables @PathVariable and @RequestParam validation
class ProductController(private val productService: ProductService) {

    private val log = LoggerFactory.getLogger(javaClass)

    // ── GET /api/products ────────────────────────────────────────────────────
    // Paginated list with optional filters

    @GetMapping
    fun list(
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) @DecimalMin("0") maxPrice: BigDecimal?,
        @PageableDefault(size = 20, sort = ["name"]) pageable: Pageable,
    ): Page<ProductResponse> {
        log.info(
            "[ProductController] list: category={}, maxPrice={}, page={}",
            category, maxPrice, pageable.pageNumber,
        )
        return productService.findAll(category, maxPrice, pageable)
    }

    // ── GET /api/products/{id} ────────────────────────────────────────────────

    @GetMapping("/{id}")
    fun getById(
        @PathVariable @NotBlank @Size(min = 4, max = 10) id: String,
    ): ProductResponse {
        log.info("[ProductController] getById: id={}", id)
        return productService.findById(id)   // throws ProductNotFoundException if not found
    }

    // ── POST /api/products ────────────────────────────────────────────────────

    @PostMapping
    // @ResponseStatus is intentionally omitted here: the method returns ResponseEntity,
    // which controls the status code directly via ResponseEntity.created(...).
    // Combining @ResponseStatus with ResponseEntity is redundant — ResponseEntity wins.
    fun create(@Valid @RequestBody request: CreateProductRequest): ResponseEntity<ProductResponse> {
        log.info("[ProductController] create: name={}, category={}", request.name, request.category)
        val created = productService.create(request)
        val location = URI.create("/api/products/${created.id}")
        return ResponseEntity.created(location).body(created)
    }

    // ── PUT /api/products/{id} ────────────────────────────────────────────────

    @PutMapping("/{id}")
    fun update(
        @PathVariable @NotBlank id: String,
        @Valid @RequestBody request: UpdateProductRequest,
    ): ProductResponse {
        log.info("[ProductController] update: id={}", id)
        return productService.update(id, request)
    }

    // ── DELETE /api/products/{id} ─────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable @NotBlank id: String) {
        log.info("[ProductController] delete: id={}", id)
        productService.delete(id)
    }

    // ── POST /api/products/{id}/stock ─────────────────────────────────────────

    @PostMapping("/{id}/stock")
    fun adjustStock(
        @PathVariable @NotBlank id: String,
        @Valid @RequestBody request: StockAdjustmentRequest,
    ): StockResponse {
        log.info("[ProductController] adjustStock: id={}, delta={}", id, request.delta)
        return productService.adjustStock(id, request.delta)
    }

    // ── POST /api/products/import ─────────────────────────────────────────────
    // Non-blocking import — returns immediately with a job ID

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun importProducts(@Valid @RequestBody products: List<CreateProductRequest>): ImportJobResponse {
        log.info("[ProductController] importProducts: {} items submitted", products.size)
        val jobId = productService.importAsync(products)
        return ImportJobResponse(jobId, "ACCEPTED", Instant.now())
    }
}


// ── DTOs ──────────────────────────────────────────────────────────────────────
// Kotlin data classes replace Java records.
// IMPORTANT: validation annotations on data class properties need the @field: prefix
// to target the backing field (not the constructor parameter). Without it,
// the annotation is placed on the constructor parameter and Bean Validation ignores it.

data class CreateProductRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    val name: String,

    @field:NotBlank(message = "Category is required")
    @field:Pattern(regexp = "outerwear|tops|bottoms|accessories", message = "Invalid category")
    val category: String,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "1", message = "Price must be at least ¥1")
    val priceYen: BigDecimal,

    @field:NotNull(message = "Initial stock is required")
    @field:Min(value = 0, message = "Stock cannot be negative")
    val initialStock: Int,
)

data class UpdateProductRequest(
    @field:Size(min = 2, max = 100) val name: String?,
    @field:DecimalMin("1") val priceYen: BigDecimal?,
)

data class StockAdjustmentRequest(
    @field:NotNull
    @field:Min(-10000)
    @field:Max(10000)
    val delta: Int,
)

data class ProductResponse(
    val id: String,
    val name: String,
    val category: String,
    val priceYen: BigDecimal,
    val stock: Int,
    val updatedAt: Instant,
)

data class StockResponse(val productId: String, val newStock: Int)

data class ImportJobResponse(val jobId: String, val status: String, val submittedAt: Instant)


// ── Stub Service (illustrative) ───────────────────────────────────────────────

interface ProductService {
    fun findAll(category: String?, maxPrice: BigDecimal?, pageable: Pageable): Page<ProductResponse>
    fun findById(id: String): ProductResponse
    fun create(request: CreateProductRequest): ProductResponse
    fun update(id: String, request: UpdateProductRequest): ProductResponse
    fun delete(id: String)
    fun adjustStock(id: String, delta: Int): StockResponse
    fun importAsync(products: List<CreateProductRequest>): String   // returns jobId
}

// ── Stub Implementation (wires up context without a real DB) ──────────────────

@org.springframework.stereotype.Service
class StubProductService : ProductService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun findAll(category: String?, maxPrice: BigDecimal?, pageable: Pageable): Page<ProductResponse> =
        org.springframework.data.domain.PageImpl(emptyList())

    override fun findById(id: String): ProductResponse {
        log.info("[StubProductService] findById: {}", id)
        throw ProductNotFoundException(id)
    }

    override fun create(request: CreateProductRequest): ProductResponse {
        log.info("[StubProductService] create: {}", request.name)
        return ProductResponse(
            id = "J${System.currentTimeMillis()}",
            name = request.name,
            category = request.category,
            priceYen = request.priceYen,
            stock = request.initialStock,
            updatedAt = Instant.now(),
        )
    }

    override fun update(id: String, request: UpdateProductRequest): ProductResponse {
        throw ProductNotFoundException(id)
    }

    override fun delete(id: String) {
        log.info("[StubProductService] delete: {}", id)
    }

    override fun adjustStock(id: String, delta: Int): StockResponse =
        StockResponse(id, 100 + delta)

    override fun importAsync(products: List<CreateProductRequest>): String {
        log.info("[StubProductService] importAsync: {} items", products.size)
        return "job-${System.currentTimeMillis()}"
    }
}
