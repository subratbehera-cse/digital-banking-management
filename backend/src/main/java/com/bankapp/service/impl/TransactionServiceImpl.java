package com.bankapp.service.impl;

import com.bankapp.dto.request.DepositRequest;
import com.bankapp.dto.request.TransferRequest;
import com.bankapp.dto.request.WithdrawRequest;
import com.bankapp.dto.response.PageResponse;
import com.bankapp.dto.response.TransactionDto;
import com.bankapp.entity.*;
import com.bankapp.exception.InsufficientBalanceException;
import com.bankapp.exception.InvalidOperationException;
import com.bankapp.exception.ResourceNotFoundException;
import com.bankapp.exception.UnauthorizedAccessException;
import com.bankapp.mapper.TransactionMapper;
import com.bankapp.repository.BankAccountRepository;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.service.TransactionService;
import com.bankapp.util.TransactionRefGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Core financial engine of the application. All money-movement operations
 * are wrapped in a database transaction (@Transactional) so that a balance
 * update and its corresponding transaction record either both succeed or
 * both roll back together. Pessimistic row locks (findByIdForUpdate) are
 * used on account rows to prevent race conditions during concurrent transfers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final BankAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FailedTransactionRecorder failedTransactionRecorder;

    @Override
    @Transactional
    public TransactionDto deposit(Long userId, DepositRequest request) {
        BankAccount account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + request.getAccountId()));

        assertOwnership(account, userId);
        assertActive(account);

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction txn = Transaction.builder()
                .transactionRef(TransactionRefGenerator.generate())
                .toAccount(account)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .amount(request.getAmount())
                .balanceAfter(account.getBalance())
                .description(orDefault(request.getDescription(), "Cash deposit"))
                .build();
        Transaction saved = transactionRepository.save(txn);

        log.info("Deposit of {} completed on account {}", request.getAmount(), account.getAccountNumber());
        return TransactionMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TransactionDto withdraw(Long userId, WithdrawRequest request) {
        BankAccount account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + request.getAccountId()));

        assertOwnership(account, userId);
        assertActive(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            recordFailedTransaction(account, null, TransactionType.WITHDRAWAL, request.getAmount(),
                    "Insufficient balance");
            throw new InsufficientBalanceException(
                    "Insufficient balance in account " + account.getAccountNumber() + " for this withdrawal");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        Transaction txn = Transaction.builder()
                .transactionRef(TransactionRefGenerator.generate())
                .fromAccount(account)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .amount(request.getAmount())
                .balanceAfter(account.getBalance())
                .description(orDefault(request.getDescription(), "Cash withdrawal"))
                .build();
        Transaction saved = transactionRepository.save(txn);

        log.info("Withdrawal of {} completed on account {}", request.getAmount(), account.getAccountNumber());
        return TransactionMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TransactionDto transfer(Long userId, TransferRequest request) {
        BankAccount source = accountRepository.findByIdForUpdate(request.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        assertOwnership(source, userId);
        assertActive(source);

        BankAccount destination = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination account not found: " + request.getToAccountNumber()));

        if (source.getId().equals(destination.getId())) {
            throw new InvalidOperationException("Cannot transfer money to the same account");
        }
        if (destination.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidOperationException("Destination account is not active");
        }

        if (source.getBalance().compareTo(request.getAmount()) < 0) {
            recordFailedTransaction(source, destination, TransactionType.TRANSFER, request.getAmount(),
                    "Insufficient balance");
            throw new InsufficientBalanceException(
                    "Insufficient balance in account " + source.getAccountNumber() + " for this transfer");
        }

        // Lock destination account row too, ordering locks by id to avoid deadlocks
        BankAccount lockedDestination = accountRepository.findByIdForUpdate(destination.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

        source.setBalance(source.getBalance().subtract(request.getAmount()));
        lockedDestination.setBalance(lockedDestination.getBalance().add(request.getAmount()));

        accountRepository.save(source);
        accountRepository.save(lockedDestination);

        String ref = TransactionRefGenerator.generate();
        Transaction txn = Transaction.builder()
                .transactionRef(ref)
                .fromAccount(source)
                .toAccount(lockedDestination)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .amount(request.getAmount())
                .balanceAfter(source.getBalance())
                .description(orDefault(request.getDescription(), "Fund transfer"))
                .build();
        Transaction saved = transactionRepository.save(txn);

        log.info("Transfer of {} from {} to {} completed with ref {}",
                request.getAmount(), source.getAccountNumber(), lockedDestination.getAccountNumber(), ref);
        return TransactionMapper.toDto(saved);
    }

    @Override
    public PageResponse<TransactionDto> getAccountTransactions(Long accountId, Long userId, int page, int size) {
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        assertOwnership(account, userId);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Transaction> result = transactionRepository.findByAccountId(accountId, pageRequest);
        return PageResponse.from(result.map(TransactionMapper::toDto));
    }

    @Override
    public PageResponse<TransactionDto> getMyTransactions(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> result = transactionRepository.findByUserId(userId, pageRequest);
        return PageResponse.from(result.map(TransactionMapper::toDto));
    }

    @Override
    public PageResponse<TransactionDto> getAllTransactions(TransactionType type, TransactionStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> result = transactionRepository.filter(type, status, pageRequest);
        return PageResponse.from(result.map(TransactionMapper::toDto));
    }

    private void recordFailedTransaction(BankAccount from, BankAccount to, TransactionType type,
                                          BigDecimal amount, String reason) {
        // Runs in its own REQUIRES_NEW transaction so the audit record of a failed
        // attempt survives even though the calling transaction is about to roll back.
        failedTransactionRecorder.record(from, to, type, amount, reason);
    }

    private void assertOwnership(BankAccount account, Long userId) {
        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to access this account");
        }
    }

    private void assertActive(BankAccount account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "Account " + account.getAccountNumber() + " is " + account.getStatus().name().toLowerCase()
                            + " and cannot be used for transactions");
        }
    }

    private String orDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
