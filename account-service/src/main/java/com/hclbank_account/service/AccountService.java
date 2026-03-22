package com.hclbank_account.service;

import com.hclbank_account.dto.*;
import com.hclbank_account.entity.*;
import com.hclbank_account.exception.*;
import com.hclbank_account.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository     accountRepository;
    private final TransactionRepository  transactionRepository;

    // GET /accounts — slide 3 product groups
    public List<AccountSummaryResponse> getAllAccounts(UUID customerId) {
        return accountRepository
                .findByCustomerId(customerId)
                .stream().map(this::toSummary)
                .collect(Collectors.toList());
    }

    // GET /accounts/{id} — slide 5 account detail header
    public AccountDetailResponse getAccountDetail(
            UUID accountId, UUID customerId) {
        Account acc = accountRepository
                .findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found or does not belong to you"));
        return toDetail(acc);
    }

    // GET /accounts/{id}/statements — slide 5 statement list
    public List<StatementResponse> getStatements(
            UUID accountId, UUID customerId, int limit) {
        // Verify ownership first
        accountRepository
                .findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new UnauthorizedAccessException(
                        "No access to this account"));
        return transactionRepository
                .findByOriginAccountIdOrderByInitiatedAtDesc(
                        accountId, PageRequest.of(0, limit))
                .stream()
                .map(t -> toStatement(t, accountId))
                .collect(Collectors.toList());
    }

    // GET /accounts/{id}/statements/{txId} — drill-down
    public StatementDetailResponse getStatementDetail(
            UUID accountId, UUID txId, UUID customerId) {
        Account acc = accountRepository
                .findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new UnauthorizedAccessException(
                        "No access to this account"));
        Transaction tx = transactionRepository
                .findByIdAndOriginAccountId(txId, accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Transaction not found"));
        return toStatementDetail(tx, acc);
    }

    // GET /internal/accounts/{id}/balance — Transfer service Feign call
    public BalanceResponse getBalance(UUID accountId) {
        Account acc = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountId));
        return new BalanceResponse(acc.getBalance(), acc.getCurrency());
    }

    // ── MAPPERS ──────────────────────────────────────────────────────────
    private AccountSummaryResponse toSummary(Account a) {
        return AccountSummaryResponse.builder()
                .id(a.getId()).accountNumber(a.getAccountNumber())
                .name(a.getName()).balance(a.getBalance())
                .currency(a.getCurrency()).build();
    }

    private AccountDetailResponse toDetail(Account a) {
        return AccountDetailResponse.builder()
                .id(a.getId()).accountNumber(a.getAccountNumber())
                .ifscCode(a.getIfscCode()).name(a.getName())
                .balance(a.getBalance()).currency(a.getCurrency())
                .createdAt(a.getCreatedAt()).build();
    }

    private StatementResponse toStatement(Transaction t, UUID accId) {
        String dir = t.getOriginAccountId().equals(accId)
                ? "DEBIT" : "CREDIT";
        return StatementResponse.builder()
                .id(t.getId()).comment(t.getComment())
                .amount(t.getAmount()).direction(dir)
                .status(t.getStatus()).beneficiaryName(t.getBeneficiaryName())
                .initiatedAt(t.getInitiatedAt()).build();
    }

    private StatementDetailResponse toStatementDetail(
            Transaction t, Account a) {
        String dir = t.getOriginAccountId().equals(a.getId())
                ? "DEBIT" : "CREDIT";
        return StatementDetailResponse.builder()
                .id(t.getId()).comment(t.getComment())
                .amount(t.getAmount()).currency(t.getCurrency())
                .direction(dir).status(t.getStatus())
                .failureReason(t.getFailureReason())
                .beneficiaryName(t.getBeneficiaryName())
                .originAccountNumber(a.getAccountNumber())
                .initiatedAt(t.getInitiatedAt())
                .completedAt(t.getCompletedAt()).build();
    }

    // ── POST /internal/accounts/create ────────────────────────────────────
// Called by Auth service via Feign right after customer signup
// Creates the account row in account_db
    @Transactional
    public void createAccount(CreateAccountRequest req) {
        Account account = Account.builder()
                .customerId(req.getCustomerId())
                .accountNumber(req.getAccountNumber())
                .ifscCode(req.getIfscCode())
                .name(req.getName() != null
                        ? req.getName() : "Savings account")
                .balance(java.math.BigDecimal.ZERO)
                .currency("INR")
                .build();

        accountRepository.save(account);
        log.info("Account created for customer: {}", req.getCustomerId());
    }
}