package com.example.banking.repository;

import com.example.banking.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByUserId(Long userId);

    /**
     * Explicit JOIN FETCH so the owning user is loaded eagerly for this
     * query alone (avoids N+1 selects when we need account + owner name
     * together), while the entity mapping itself stays LAZY by default.
     */
    @Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.id = :accountId")
    Optional<Account> findByIdWithUser(@Param("accountId") Long accountId);
}
