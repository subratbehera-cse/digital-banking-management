package com.bankapp.controller;

import com.bankapp.dto.response.*;
import com.bankapp.entity.AccountStatus;
import com.bankapp.entity.TransactionStatus;
import com.bankapp.entity.TransactionType;
import com.bankapp.service.AccountService;
import com.bankapp.service.AdminService;
import com.bankapp.service.DashboardStatsDto;
import com.bankapp.service.TransactionService;
import com.bankapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * All endpoints in this controller are restricted to ROLE_ADMIN via SecurityConfig
 * (/api/admin/**) and reinforced at method level with @PreAuthorize.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardStatsDto> getDashboard() {
        return ApiResponse.success(adminService.getDashboardStats());
    }

    @GetMapping("/customers")
    public ApiResponse<PageResponse<UserDto>> getCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.searchCustomers(keyword, page, size));
    }

    @PatchMapping("/customers/{userId}/status")
    public ApiResponse<UserDto> setCustomerStatus(@PathVariable Long userId, @RequestParam boolean enabled) {
        String message = enabled ? "Customer account activated" : "Customer account deactivated";
        return ApiResponse.success(message, userService.setEnabled(userId, enabled));
    }

    @GetMapping("/accounts")
    public ApiResponse<PageResponse<AccountDto>> getAccounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(accountService.searchAccounts(keyword, page, size));
    }

    @GetMapping("/accounts/status/{status}")
    public ApiResponse<PageResponse<AccountDto>> getAccountsByStatus(
            @PathVariable AccountStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(accountService.getAccountsByStatus(status, page, size));
    }

    @PatchMapping("/accounts/{accountId}/status")
    public ApiResponse<AccountDto> updateAccountStatus(@PathVariable Long accountId,
                                                        @RequestParam AccountStatus status) {
        return ApiResponse.success("Account status updated", accountService.updateStatus(accountId, status));
    }

    @GetMapping("/transactions")
    public ApiResponse<PageResponse<TransactionDto>> getAllTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(transactionService.getAllTransactions(type, status, page, size));
    }
}
