package com.hclbank_auth.service;

import com.hclbank_auth.client.AccountServiceClient;
import com.hclbank_auth.config.JwtUtil;
import com.hclbank_auth.dto.*;
import com.hclbank_auth.entity.Customer;
import com.hclbank_auth.exception.*;
import com.hclbank_auth.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final CustomerRepository  customerRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtUtil              jwtUtil;
    private final AccountServiceClient accountServiceClient;

    // ── SIGNUP ──────────────────────────────────────────────────────
    @Transactional
    public SignupResponse signup(SignupRequest req) {

        // 1. Check duplicate email
        if (customerRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateEmailException(
                    "Email " + req.getEmail() + " is already registered");
        }

        // 2. Generate customer ID
        long count = customerRepository.count();
        String customerId = String.format("CUST%06d", count + 1);

        // 3. Hash password — NEVER store plain text
        String passwordHash = passwordEncoder.encode(req.getPassword());

        // 4. Build and save Customer entity
        Customer customer = Customer.builder()
                .customerId(customerId)
                .passwordHash(passwordHash)
                .fullName(req.getFullName())
                .email(req.getEmail().toLowerCase().trim())
                .phone(req.getPhone())
                .isActive(true)
                .build();

        customerRepository.save(customer);
        log.info("Customer saved: {}", customerId);

        // 5. Call account service via Feign to create account row
        // MUST be before return — code after return never executes
        accountServiceClient.createAccount(
                new CreateAccountRequest(
                        customer.getId(),
                        req.getAccountNumber(),
                        req.getIfscCode(),
                        "Savings account"
                )
        );
        log.info("Account created in account-service for: {}", customerId);

        // 6. Issue JWT
        String token = jwtUtil.generateToken(customerId, customer.getId());

        // 7. return is the LAST statement — always
        return new SignupResponse(
                customerId,
                token,
                "Account created! Your Customer ID is "
                        + customerId + ". Save it — you need it to login."
        );
    }

    // ── LOGIN ────────────────────────────────────────────────────────
    @Transactional
    public LoginResponse login(LoginRequest req) {

        // 1. Find customer by customer_id
        Customer customer = customerRepository
                .findByCustomerId(req.getCustomerId())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid Customer ID or password"));

        // 2. Check account is active
        if (!customer.isActive()) {
            throw new AccountInactiveException(
                    "Account inactive. Contact support.");
        }

        // 3. BCrypt compare — typed password vs stored hash
        if (!passwordEncoder.matches(
                req.getPassword(), customer.getPasswordHash())) {
            throw new InvalidCredentialsException(
                    "Invalid Customer ID or password");
        }

        // 4. Update last login timestamp
        customerRepository.updateLastLogin(
                customer.getCustomerId(), LocalDateTime.now());

        // 5. Issue JWT
        String token = jwtUtil.generateToken(
                customer.getCustomerId(), customer.getId());

        log.info("Login successful: {}", customer.getCustomerId());

        // 6. return is LAST
        return new LoginResponse(
                token,
                customer.getCustomerId(),
                customer.getFullName(),
                1800
        );
    }
}