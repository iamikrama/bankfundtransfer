package com.hclbank_account.dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    // Customer UUID from auth_db — sent by auth service after signup
    private UUID   customerId;
    private String accountNumber;
    private String ifscCode;
    private String name;  // e.g. "Savings account"
}