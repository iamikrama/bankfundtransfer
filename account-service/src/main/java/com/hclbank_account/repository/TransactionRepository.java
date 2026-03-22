package com.hclbank_account.repository;

import com.hclbank_account.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    // Last N transactions ordered by date DESC
    // Pageable controls the limit — controller passes PageRequest.of(0, limit)
    List<Transaction> findByOriginAccountIdOrderByInitiatedAtDesc(
            UUID originAccountId, Pageable pageable);

    // Verify transaction belongs to this account before drill-down
    Optional<Transaction> findByIdAndOriginAccountId(
            UUID id, UUID originAccountId);
}