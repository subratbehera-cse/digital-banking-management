package com.bankapp.controller;

import com.bankapp.dto.request.CreateAccountRequest;
import com.bankapp.dto.response.AccountDto;
import com.bankapp.dto.response.ApiResponse;
import com.bankapp.security.UserPrincipal;
import com.bankapp.service.AccountService;
import com.bankapp.service.TransactionService;
import com.bankapp.dto.response.PageResponse;
import com.bankapp.dto.response.TransactionDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @Valid @RequestBody CreateAccountRequest request) {
        AccountDto account = accountService.createAccount(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", account));
    }

    @GetMapping
    public ApiResponse<List<AccountDto>> getMyAccounts(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(accountService.getMyAccounts(principal.getId()));
    }

    @GetMapping("/{accountId}")
    public ApiResponse<AccountDto> getAccount(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable Long accountId) {
        return ApiResponse.success(accountService.getAccountForOwner(accountId, principal.getId()));
    }

    @GetMapping("/{accountId}/transactions")
    public ApiResponse<PageResponse<TransactionDto>> getAccountTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(
                transactionService.getAccountTransactions(accountId, principal.getId(), page, size));
    }
}
