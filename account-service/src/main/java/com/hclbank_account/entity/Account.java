package com.hclbank_account.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Customer UUID from auth_db — different DB so no @JoinColumn
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "account_number", unique = true,
            nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "ifsc_code", nullable = false, length = 15)
    private String ifscCode;

    @Column(length = 100)
    private String name;

    // BigDecimal — NEVER Double for money amounts
    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(length = 5)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}