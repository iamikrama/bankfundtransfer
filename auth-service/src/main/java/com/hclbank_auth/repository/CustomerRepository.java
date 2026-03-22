package com.hclbank_auth.repository;

import com.hclbank_auth.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository
        extends JpaRepository<Customer, UUID> {

    // Login — customer types customer_id
    Optional<Customer> findByCustomerId(String customerId);

    // Signup — check email not already used
    boolean existsByEmail(String email);

    // Update last_login_at on every successful login
    @Modifying
    @Query("UPDATE Customer c SET c.lastLoginAt = :time " +
            "WHERE c.customerId = :cid")
    void updateLastLogin(@Param("cid") String customerId,
                         @Param("time") LocalDateTime time);
}