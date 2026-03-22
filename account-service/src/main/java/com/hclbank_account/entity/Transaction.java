package com.hclbank_account.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "origin_account_id", nullable = false)
    private UUID originAccountId;

    @Column(name = "beneficiary_id", nullable = false)
    private UUID beneficiaryId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 5)
    @Builder.Default
    private String currency = "INR";

    @Column(nullable = false, length = 140)
    private String comment;

    // INITIATED | COMPLETED | FAILED | ROLLED_BACK
    @Column(nullable = false, length = 15)
    private String status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    // Denormalised — stored so statement list never needs cross-service call
    @Column(name = "beneficiary_name", length = 100)
    private String beneficiaryName;

    @Column(name = "initiated_at", updatable = false)
    @Builder.Default
    private LocalDateTime initiatedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}