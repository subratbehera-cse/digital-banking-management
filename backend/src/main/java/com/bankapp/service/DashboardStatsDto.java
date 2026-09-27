package com.bankapp.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalCustomers;
    private long totalAccounts;
    private long activeAccounts;
    private long inactiveAccounts;
    private BigDecimal totalBankBalance;
    private long totalTransactions;
    private long transactionsToday;
}
