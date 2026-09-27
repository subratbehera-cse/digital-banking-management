package com.bankapp.repository;

import com.bankapp.entity.AccountStatus;
import com.bankapp.entity.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findByUserId(Long userId);

    Page<BankAccount> findByUserId(Long userId, Pageable pageable);

    boolean existsByAccountNumber(String accountNumber);

    Page<BankAccount> findByStatus(AccountStatus status, Pageable pageable);

    @Query("select a from BankAccount a where lower(a.accountNumber) like lower(concat('%', :keyword, '%')) " +
           "or lower(a.user.username) like lower(concat('%', :keyword, '%')) " +
           "or lower(a.user.email) like lower(concat('%', :keyword, '%'))")
    Page<BankAccount> search(@Param("keyword") String keyword, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BankAccount a where a.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);
}
