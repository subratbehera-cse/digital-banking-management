package com.bankapp.repository;

import com.bankapp.entity.Transaction;
import com.bankapp.entity.TransactionStatus;
import com.bankapp.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionRef(String ref);

    @Query("select t from Transaction t where t.fromAccount.id = :accountId or t.toAccount.id = :accountId " +
           "order by t.createdAt desc")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("select t from Transaction t where (t.fromAccount.user.id = :userId or t.toAccount.user.id = :userId)")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    Page<Transaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select t from Transaction t where " +
           "(:type is null or t.type = :type) and (:status is null or t.status = :status)")
    Page<Transaction> filter(@Param("type") TransactionType type,
                              @Param("status") TransactionStatus status,
                              Pageable pageable);
}
