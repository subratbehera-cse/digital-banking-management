package com.bankapp.service.impl;

import com.bankapp.entity.BankAccount;
import com.bankapp.entity.Transaction;
import com.bankapp.entity.TransactionStatus;
import com.bankapp.entity.TransactionType;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.util.TransactionRefGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Persists an audit trail of failed transaction attempts (e.g. insufficient balance)
 * using Propagation.REQUIRES_NEW, so the audit record commits independently of the
 * outer transaction which is going to be rolled back and re-thrown to the caller.
 */
@Component
@RequiredArgsConstructor
public class FailedTransactionRecorder {

    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(BankAccount from, BankAccount to, TransactionType type, BigDecimal amount, String reason) {
        Transaction failed = Transaction.builder()
                .transactionRef(TransactionRefGenerator.generate())
                .fromAccount(from)
                .toAccount(to)
                .type(type)
                .status(TransactionStatus.FAILED)
                .amount(amount)
                .balanceAfter(from != null ? from.getBalance() : null)
                .description("Failed " + type.name().toLowerCase())
                .failureReason(reason)
                .build();
        transactionRepository.save(failed);
    }
}
