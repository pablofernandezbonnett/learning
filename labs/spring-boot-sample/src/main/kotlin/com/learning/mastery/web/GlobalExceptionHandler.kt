package com.learning.mastery.web

import com.learning.mastery.web.exceptions.InsufficientStockException
import com.learning.mastery.web.exceptions.ProductNotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.net.URI

/**
 * GlobalExceptionHandler — centralized exception handling for all @RestControllers.
 *
 * Returns RFC 9457 ProblemDetail responses. Enable in application.yml:
 *   spring.mvc.problemdetails.enabled=true
 *
 * Response format:
 * {
 *   "type": "about:blank",
 *   "title": "Product Not Found",
 *   "status": 404,
 *   "detail": "Product 'J999' does not exist in this store",
 *   "instance": "/api/products/J999",
 *   "productId": "J999"
 * }
 *
 * Kotlin idiom: exception properties accessed directly (ex.productId)
 * instead of Java-style ex.getProductId().
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    // ── Domain / Business Exceptions ─────────────────────────────────────────

    @ExceptionHandler(ProductNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleProductNotFound(ex: ProductNotFoundException, request: HttpServletRequest): ProblemDetail {
        log.warn("[GlobalExceptionHandler] Product not found: productId={}", ex.productId)
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Product '${ex.productId}' does not exist in this store",
        ).apply {
            title = "Product Not Found"
            instance = URI.create(request.requestURI)
            setProperty("productId", ex.productId)
        }
    }

    @ExceptionHandler(InsufficientStockException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleInsufficientStock(ex: InsufficientStockException, request: HttpServletRequest): ProblemDetail {
        log.warn(
            "[GlobalExceptionHandler] Insufficient stock: productId={}, requested={}, available={}",
            ex.productId, ex.requested, ex.available,
        )
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "Requested quantity (${ex.requested}) exceeds available stock (${ex.available})",
        ).apply {
            title = "Insufficient Stock"
            instance = URI.create(request.requestURI)
            setProperty("productId", ex.productId)
            setProperty("requested", ex.requested)
            setProperty("available", ex.available)
        }
    }

    // ── Validation Exceptions ────────────────────────────────────────────────

    /**
     * Triggered by @Valid / @Validated on @RequestBody.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ProblemDetail {
        val errorCount = ex.bindingResult.errorCount
        log.warn("[GlobalExceptionHandler] Request body validation failed: {} error(s)", errorCount)

        val errors = linkedMapOf<String, String?>()
        ex.bindingResult.fieldErrors.forEach { fe -> errors[fe.field] = fe.defaultMessage }
        ex.bindingResult.globalErrors.forEach { ge -> errors[ge.objectName] = ge.defaultMessage }

        return ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY).apply {
            title = "Validation Failed"
            detail = "$errorCount validation error(s)"
            setProperty("errors", errors)
        }
    }

    /**
     * Triggered by @Validated on @PathVariable or @RequestParam.
     */
    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleConstraintViolation(ex: ConstraintViolationException): ProblemDetail {
        log.warn("[GlobalExceptionHandler] Constraint violation: {}", ex.message)

        val errors = ex.constraintViolations.associate { v ->
            v.propertyPath.toString() to v.message
        }

        return ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY).apply {
            title = "Constraint Violation"
            detail = "${ex.constraintViolations.size} constraint(s) violated"
            setProperty("errors", errors)
        }
    }

    /**
     * Triggered by a @PathVariable with wrong type (e.g., /products/abc when expecting Long).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException, request: HttpServletRequest): ProblemDetail {
        log.warn(
            "[GlobalExceptionHandler] Type mismatch: param='{}' value='{}' expected={}",
            ex.name, ex.value, ex.requiredType?.simpleName ?: "unknown",
        )
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Parameter '${ex.name}' must be of type ${ex.requiredType?.simpleName ?: "unknown"}",
        ).apply {
            title = "Bad Request"
            instance = URI.create(request.requestURI)
            setProperty("parameter", ex.name)
            setProperty("value", ex.value?.toString())
        }
    }

    // ── Security Exceptions ──────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccessDenied(ex: AccessDeniedException, request: HttpServletRequest): ProblemDetail {
        log.warn(
            "[GlobalExceptionHandler] Access denied for {} {}: {}",
            request.method, request.requestURI, ex.message,
        )
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "You do not have permission to perform this action",
        ).apply {
            title = "Forbidden"
            instance = URI.create(request.requestURI)
        }
    }

    // ── Catch-All ────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGenericException(ex: Exception, request: HttpServletRequest): ProblemDetail {
        log.error(
            "[GlobalExceptionHandler] Unhandled exception on {} {}: {}",
            request.method, request.requestURI, ex.message, ex,
        )
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later.",
        ).apply {
            title = "Internal Server Error"
            instance = URI.create(request.requestURI)
            // Never expose internal message or stack trace to clients in production
        }
    }
}
