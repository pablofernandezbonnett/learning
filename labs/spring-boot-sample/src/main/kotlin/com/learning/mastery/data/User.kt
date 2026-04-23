package com.learning.mastery.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

/**
 * User entity — demonstrates JPA annotations in Kotlin.
 *
 * Kotlin + JPA note: JPA requires a no-arg constructor for entity proxying.
 * The kotlin("plugin.jpa") Gradle plugin generates this automatically for
 * @Entity, @MappedSuperclass, and @Embeddable classes. Without it, Hibernate
 * throws: "No default constructor for entity: User".
 *
 * All properties use 'var' (not 'val') because Hibernate needs to set them
 * after construction via the no-arg constructor.
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_email", columnList = "email", unique = true),
        Index(name = "idx_users_store_id", columnList = "store_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true, length = 255)
    var email: String = ""

    @Column(nullable = false, length = 100)
    var name: String = ""

    @Column(name = "store_id")
    var storeId: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.CUSTOMER

    @Column(nullable = false)
    var active: Boolean = true

    @Column(name = "total_purchases_yen")
    var totalPurchasesYen: Long = 0L

    // ── Auditing — automatically populated by Spring Data ─────────────────────
    // Requires @EnableJpaAuditing on @SpringBootApplication or a @Configuration class.

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant? = null

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: Instant? = null

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    var createdBy: String? = null

    @LastModifiedBy
    @Column(name = "updated_by")
    var updatedBy: String? = null
}

enum class UserRole { ADMIN, STORE_MANAGER, ASSOCIATE, CUSTOMER }
