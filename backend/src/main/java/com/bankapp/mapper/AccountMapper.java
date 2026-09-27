package com.bankapp.mapper;

import com.bankapp.dto.response.AccountDto;
import com.bankapp.entity.BankAccount;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static AccountDto toDto(BankAccount account) {
        return AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .ownerId(account.getUser().getId())
                .ownerName(account.getUser().getFirstName() + " " + account.getUser().getLastName())
                .ownerUsername(account.getUser().getUsername())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
