package com.bankapp.controller;

import com.bankapp.dto.request.DepositRequest;
import com.bankapp.dto.request.TransferRequest;
import com.bankapp.dto.request.WithdrawRequest;
import com.bankapp.dto.response.ApiResponse;
import com.bankapp.dto.response.PageResponse;
import com.bankapp.dto.response.TransactionDto;
import com.bankapp.security.UserPrincipal;
import com.bankapp.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionDto>> deposit(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @Valid @RequestBody DepositRequest request) {
        TransactionDto txn = transactionService.deposit(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Deposit successful", txn));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionDto>> withdraw(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @Valid @RequestBody WithdrawRequest request) {
        TransactionDto txn = transactionService.withdraw(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Withdrawal successful", txn));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionDto>> transfer(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @Valid @RequestBody TransferRequest request) {
        TransactionDto txn = transactionService.transfer(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Transfer successful", txn));
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<TransactionDto>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(transactionService.getMyTransactions(principal.getId(), page, size));
    }
}
