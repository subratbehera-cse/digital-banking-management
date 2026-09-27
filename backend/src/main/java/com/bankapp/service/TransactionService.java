package com.bankapp.service;

import com.bankapp.dto.request.DepositRequest;
import com.bankapp.dto.request.TransferRequest;
import com.bankapp.dto.request.WithdrawRequest;
import com.bankapp.dto.response.PageResponse;
import com.bankapp.dto.response.TransactionDto;
import com.bankapp.entity.TransactionStatus;
import com.bankapp.entity.TransactionType;

public interface TransactionService {
    TransactionDto deposit(Long userId, DepositRequest request);
    TransactionDto withdraw(Long userId, WithdrawRequest request);
    TransactionDto transfer(Long userId, TransferRequest request);
    PageResponse<TransactionDto> getAccountTransactions(Long accountId, Long userId, int page, int size);
    PageResponse<TransactionDto> getMyTransactions(Long userId, int page, int size);
    PageResponse<TransactionDto> getAllTransactions(TransactionType type, TransactionStatus status, int page, int size);
}
