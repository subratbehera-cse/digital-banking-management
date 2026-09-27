package com.bankapp.dto.response;

import com.bankapp.entity.TransactionStatus;
import com.bankapp.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private String transactionRef;
    private String fromAccountNumber;
    private String toAccountNumber;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private String failureReason;
    private LocalDateTime createdAt;
}
