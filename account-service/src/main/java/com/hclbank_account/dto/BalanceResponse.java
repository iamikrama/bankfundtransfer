package com.hclbank_account.dto;

import lombok.*; import java.math.BigDecimal;

@Data @AllArgsConstructor @NoArgsConstructor
public class BalanceResponse {
    private BigDecimal balance;
    private String     currency;
}
