package com.learning.mastery.web.exceptions

/**
 * Domain exceptions — typed exceptions that carry structured data.
 *
 * Kotlin idiom: primary constructor carries the data fields directly.
 * No need for separate field declarations + getters as in Java.
 *
 * These are caught by GlobalExceptionHandler and converted to RFC 9457
 * ProblemDetail responses.
 */

class ProductNotFoundException(val productId: String) :
    RuntimeException("Product not found: $productId")

class InsufficientStockException(
    val productId: String,
    val requested: Int,
    val available: Int,
) : RuntimeException(
    "Insufficient stock for $productId: requested=$requested, available=$available",
)
