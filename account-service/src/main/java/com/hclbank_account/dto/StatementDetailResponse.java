package com.hclbank_account.dto;

import lombok.*; import java.math.BigDecimal;
import java.time.LocalDateTime; import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class StatementDetailResponse {
    private UUID          id;
    private String        comment;
    private BigDecimal    amount;
    private String        currency;
    private String        direction;
    private String        status;
    private String        failureReason;
    private String        beneficiaryName;
    private String        originAccountNumber;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
}