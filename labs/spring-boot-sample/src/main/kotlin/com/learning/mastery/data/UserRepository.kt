package com.learning.mastery.data

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * UserRepository — demonstrates senior Spring Data JPA patterns in Kotlin.
 *
 * Patterns shown:
 * 1. Projections (interface-based) — partial data fetch, no extra columns
 * 2. Specifications — dynamic, composable WHERE clauses
 * 3. @Query with JPQL and native SQL
 * 4. @Modifying for bulk updates
 *
 * Kotlin note: Spring Data generates the implementation at runtime — the interface
 * itself needs no changes when moving from Java to Kotlin.
 */

// ── 1. Projections — fetch only what you need ─────────────────────────────────
// Interface-based projections: Spring generates a proxy that maps columns to methods.

interface UserSummary {
    fun getId(): Long
    fun getName(): String
    fun getEmail(): String
    fun getRole(): UserRole
}

interface UserEmailOnly {
    fun getEmail(): String
}

// Nested projection
interface UserWithStore {
    fun getId(): Long
    fun getName(): String
    fun getStore(): StoreInfo

    interface StoreInfo {
        fun getId(): Long
        fun getName(): String
    }
}

// ── 2. Repository ─────────────────────────────────────────────────────────────

@Repository
interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // ── Basic derived queries ────────────────────────────────────────────────

    fun findByEmail(email: String): User?                         // Kotlin nullable replaces Optional<User>

    fun findByActiveTrue(): List<User>

    fun findByStoreId(storeId: Long, pageable: Pageable): Page<User>

    fun existsByEmail(email: String): Boolean

    // ── Projection queries ───────────────────────────────────────────────────

    fun findByRole(role: UserRole): List<UserSummary>

    @Query("SELECT u FROM User u WHERE u.active = true")
    fun findEmailsByActiveTrue(): List<UserEmailOnly>  // @Query avoids Spring Data mis-parsing "Emails" as a property

    // Dynamic projection — caller specifies what fields they want
    fun <T> findByStoreId(storeId: Long, type: Class<T>): List<T>

    // ── @Query — JPQL ────────────────────────────────────────────────────────

    @Query(
        "SELECT u FROM User u WHERE u.storeId = :storeId AND u.active = true ORDER BY u.totalPurchasesYen DESC",
    )
    fun findTopCustomersByStore(@Param("storeId") storeId: Long, pageable: Pageable): List<UserSummary>

    @Query("SELECT u FROM User u WHERE u.totalPurchasesYen >= :threshold")
    fun findHighValueCustomers(@Param("threshold") thresholdYen: Long): List<UserSummary>

    // ── @Query — Native SQL ───────────────────────────────────────────────────
    // Use when JPQL can't express the query (window functions, CTEs, etc.)

    @Query(
        value = """
            SELECT u.id, u.name, u.email, u.total_purchases_yen,
                   RANK() OVER (PARTITION BY u.store_id ORDER BY u.total_purchases_yen DESC) AS rank
            FROM users u
            WHERE u.store_id = :storeId AND u.active = true
        """,
        nativeQuery = true,
    )
    fun findCustomerRankingByStore(@Param("storeId") storeId: Long): List<Array<Any>>

    // ── @Modifying — Bulk updates (no entity loading) ─────────────────────────

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.active = false WHERE u.storeId = :storeId")
    fun deactivateAllByStore(@Param("storeId") storeId: Long): Int

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.totalPurchasesYen = u.totalPurchasesYen + :amount WHERE u.id = :userId")
    fun addPurchaseAmount(@Param("userId") userId: Long, @Param("amount") amountYen: Long): Int
}

// ── 3. Specifications — composable WHERE clauses ──────────────────────────────

object UserSpecifications {

    fun isActive(): Specification<User> =
        Specification { root, _, cb -> cb.isTrue(root.get("active")) }

    fun hasRole(role: UserRole?): Specification<User> =
        Specification { root, _, cb -> role?.let { cb.equal(root.get<UserRole>("role"), it) } }

    fun inStore(storeId: Long?): Specification<User> =
        Specification { root, _, cb -> storeId?.let { cb.equal(root.get<Long>("storeId"), it) } }

    fun isHighValue(thresholdYen: Long): Specification<User> =
        Specification { root, _, cb -> cb.greaterThanOrEqualTo(root.get("totalPurchasesYen"), thresholdYen) }

    fun nameContains(keyword: String?): Specification<User> =
        Specification { root, _, cb ->
            keyword?.let { cb.like(cb.lower(root.get("name")), "%${it.lowercase()}%") }
        }
}

// Usage in a service:
//
// fun search(storeId: Long?, role: UserRole?, nameKeyword: String?, pageable: Pageable): Page<User> {
//     val spec = Specification
//         .where(UserSpecifications.isActive())
//         .and(UserSpecifications.inStore(storeId))       // null-safe — ignored if storeId is null
//         .and(UserSpecifications.hasRole(role))           // null-safe — ignored if role is null
//         .and(UserSpecifications.nameContains(nameKeyword))
//
//     log.info("[UserService] search: storeId={}, role={}, keyword={}", storeId, role, nameKeyword)
//     return userRepository.findAll(spec, pageable)
// }
