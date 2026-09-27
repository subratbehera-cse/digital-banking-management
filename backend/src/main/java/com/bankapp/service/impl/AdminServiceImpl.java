package com.bankapp.service.impl;

import com.bankapp.entity.AccountStatus;
import com.bankapp.repository.BankAccountRepository;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.repository.UserRepository;
import com.bankapp.service.AdminService;
import com.bankapp.service.DashboardStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final BankAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public DashboardStatsDto getDashboardStats() {
        long totalCustomers = userRepository.count();
        long totalAccounts = accountRepository.count();
        long activeAccounts = accountRepository.findByStatus(AccountStatus.ACTIVE, PageRequest.of(0, 1)).getTotalElements();
        long inactiveAccounts = accountRepository.findByStatus(AccountStatus.INACTIVE, PageRequest.of(0, 1)).getTotalElements();

        BigDecimal totalBalance = accountRepository.findAll().stream()
                .map(a -> a.getBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTransactions = transactionRepository.count();

        LocalDateTime startOfDay = LocalTime.MIDNIGHT.atDate(LocalDateTime.now().toLocalDate());
        long todayCount = transactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(startOfDay))
                .count();

        return DashboardStatsDto.builder()
                .totalCustomers(totalCustomers)
                .totalAccounts(totalAccounts)
                .activeAccounts(activeAccounts)
                .inactiveAccounts(inactiveAccounts)
                .totalBankBalance(totalBalance)
                .totalTransactions(totalTransactions)
                .transactionsToday(todayCount)
                .build();
    }
}
