package com.bankapp.service;

import com.bankapp.dto.request.CreateAccountRequest;
import com.bankapp.dto.response.AccountDto;
import com.bankapp.dto.response.PageResponse;
import com.bankapp.entity.AccountStatus;
import com.bankapp.entity.BankAccount;

import java.util.List;

public interface AccountService {
    AccountDto createAccount(Long userId, CreateAccountRequest request);
    List<AccountDto> getMyAccounts(Long userId);
    AccountDto getAccountForOwner(Long accountId, Long userId);
    BankAccount getAccountEntityOrThrow(Long accountId);
    PageResponse<AccountDto> searchAccounts(String keyword, int page, int size);
    PageResponse<AccountDto> getAccountsByStatus(AccountStatus status, int page, int size);
    AccountDto updateStatus(Long accountId, AccountStatus status);
}
