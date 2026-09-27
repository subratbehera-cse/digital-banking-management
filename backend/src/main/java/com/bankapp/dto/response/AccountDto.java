package com.bankapp.dto.response;

import com.bankapp.entity.AccountStatus;
import com.bankapp.entity.AccountType;
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
public class AccountDto {
    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private Long ownerId;
    private String ownerName;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
