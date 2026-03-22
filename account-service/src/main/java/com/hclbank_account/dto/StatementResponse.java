package com.hclbank_account.dto;

import lombok.*; import java.math.BigDecimal;
import java.time.LocalDateTime; import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class StatementResponse {
    private UUID          id;
    private String        comment;
    private BigDecimal    amount;
    private String        direction;      // DEBIT or CREDIT
    private String        status;
    private String        beneficiaryName;
    private LocalDateTime initiatedAt;
}