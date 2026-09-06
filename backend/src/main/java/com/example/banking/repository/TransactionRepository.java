package com.example.banking.repository;

import com.example.banking.entity.Transaction;
import com.example.banking.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    List<Transaction> findByAccountIdAndTransactionTypeOrderByCreatedAtDesc(Long accountId, TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId " +
           "AND t.createdAt BETWEEN :from AND :to ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountIdAndDateRange(@Param("accountId") Long accountId,
                                                   @Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to);

    /** Used by the admin dashboard to list every transaction across the bank. */
    List<Transaction> findAllByOrderByCreatedAtDesc();
    List<Transaction> findByAccountIdAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long accountId,
            TransactionType transactionType,
            LocalDateTime from,
            LocalDateTime to
    );
}
