package com.hclbank_account.dto;

import lombok.*; import java.math.BigDecimal;
import java.time.LocalDateTime; import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class AccountDetailResponse {
    private UUID          id;
    private String        accountNumber;
    private String        ifscCode;
    private String        name;
    private BigDecimal    balance;
    private String        currency;
    private LocalDateTime createdAt;
}