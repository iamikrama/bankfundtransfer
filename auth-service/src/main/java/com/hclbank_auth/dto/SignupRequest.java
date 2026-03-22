package com.hclbank_auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    private String fullName;

    @Email(message = "Enter a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$",
            message = "Enter valid 10-digit phone")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Minimum 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).+$",
            message = "Needs uppercase, number, special char")
    private String password;

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^\\d{9,18}$",
            message = "Account number must be 9-18 digits")
    private String accountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
            message = "Invalid IFSC. Example: SBIN0001234")
    private String ifscCode;
}