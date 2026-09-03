package com.banking.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity mapping to the {@code payees} table.
 * Kept in the persistence adapter package — domain model remains annotation-free.
 */
@Entity
@Table(
        name = "payees",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_payees_customer_account",
                columnNames = {"customer_id", "account_number"}
        )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayeeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 36)
    private String externalId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "payee_name", nullable = false)
    private String payeeName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
