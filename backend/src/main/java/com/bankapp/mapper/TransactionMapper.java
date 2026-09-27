package com.bankapp.mapper;

import com.bankapp.dto.response.TransactionDto;
import com.bankapp.entity.Transaction;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionDto toDto(Transaction txn) {
        return TransactionDto.builder()
                .id(txn.getId())
                .transactionRef(txn.getTransactionRef())
                .fromAccountNumber(txn.getFromAccount() != null ? txn.getFromAccount().getAccountNumber() : null)
                .toAccountNumber(txn.getToAccount() != null ? txn.getToAccount().getAccountNumber() : null)
                .type(txn.getType())
                .status(txn.getStatus())
                .amount(txn.getAmount())
                .balanceAfter(txn.getBalanceAfter())
                .description(txn.getDescription())
                .failureReason(txn.getFailureReason())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}
