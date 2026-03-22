package com.hclbank_account.repository;

import com.hclbank_account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    // GET /accounts — all accounts for customer
    List<Account> findByCustomerId(UUID customerId);

    // GET /accounts/{id} — ownership check built into query
    // Returns empty if account exists but belongs to different customer
    Optional<Account> findByIdAndCustomerId(UUID id, UUID customerId);
}
