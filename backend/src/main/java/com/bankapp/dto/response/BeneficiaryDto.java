package com.bankapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryDto {
    private Long id;
    private String beneficiaryName;
    private String beneficiaryAccountNumber;
    private String bankName;
    private String nickname;
    private LocalDateTime createdAt;
}
