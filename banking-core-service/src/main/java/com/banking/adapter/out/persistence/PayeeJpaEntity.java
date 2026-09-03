package com.banking.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity for the payees table.
 */
@Entity
@Table(
        name = "payees",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_payees_customer_account",
                        columnNames = {"customer_id", "account_number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayeeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 36)
    private String externalId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "payee_name", nullable = false, length = 255)
    private String payeeName;

    @Column(name = "account_number", nullable = false, length = 100)
    private String accountNumber;

    @Column(name = "bank_code", nullable = false, length = 50)
    private String bankCode;

    @Column(name = "bank_name", length = 255)
    private String bankName;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}