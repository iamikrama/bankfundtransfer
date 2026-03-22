package com.hclbank_account.dto;

import lombok.*; import java.math.BigDecimal; import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class AccountSummaryResponse {
    private UUID       id;
    private String     accountNumber;
    private String     name;
    private BigDecimal balance;
    private String     currency;
}