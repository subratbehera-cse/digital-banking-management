package com.bankapp.service.impl;

import com.bankapp.dto.request.CreateAccountRequest;
import com.bankapp.dto.response.AccountDto;
import com.bankapp.dto.response.PageResponse;
import com.bankapp.entity.AccountStatus;
import com.bankapp.entity.BankAccount;
import com.bankapp.entity.User;
import com.bankapp.exception.ResourceNotFoundException;
import com.bankapp.exception.UnauthorizedAccessException;
import com.bankapp.mapper.AccountMapper;
import com.bankapp.repository.BankAccountRepository;
import com.bankapp.service.AccountService;
import com.bankapp.service.UserService;
import com.bankapp.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final BankAccountRepository accountRepository;
    private final UserService userService;

    @Override
    @Transactional
    public AccountDto createAccount(Long userId, CreateAccountRequest request) {
        User user = userService.getUserEntityOrThrow(userId);

        String accountNumber;
        do {
            accountNumber = AccountNumberGenerator.generate();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        BankAccount account = BankAccount.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(request.getInitialDeposit() == null ? java.math.BigDecimal.ZERO : request.getInitialDeposit())
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();

        BankAccount saved = accountRepository.save(account);
        log.info("New {} account created: {} for user {}", request.getAccountType(), accountNumber, user.getUsername());
        return AccountMapper.toDto(saved);
    }

    @Override
    public List<AccountDto> getMyAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDto getAccountForOwner(Long accountId, Long userId) {
        BankAccount account = getAccountEntityOrThrow(accountId);
        assertOwnership(account, userId);
        return AccountMapper.toDto(account);
    }

    @Override
    public BankAccount getAccountEntityOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    @Override
    public PageResponse<AccountDto> searchAccounts(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BankAccount> result = (keyword == null || keyword.isBlank())
                ? accountRepository.findAll(pageRequest)
                : accountRepository.search(keyword, pageRequest);
        return PageResponse.from(result.map(AccountMapper::toDto));
    }

    @Override
    public PageResponse<AccountDto> getAccountsByStatus(AccountStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BankAccount> result = accountRepository.findByStatus(status, pageRequest);
        return PageResponse.from(result.map(AccountMapper::toDto));
    }

    @Override
    @Transactional
    public AccountDto updateStatus(Long accountId, AccountStatus status) {
        BankAccount account = getAccountEntityOrThrow(accountId);
        account.setStatus(status);
        BankAccount saved = accountRepository.save(account);
        log.info("Account {} status changed to {}", account.getAccountNumber(), status);
        return AccountMapper.toDto(saved);
    }

    public void assertOwnership(BankAccount account, Long userId) {
        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to access this account");
        }
    }
}
