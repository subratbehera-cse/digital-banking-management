package com.bankapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBeneficiaryRequest {

    @NotBlank(message = "Beneficiary name is required")
    private String beneficiaryName;

    @NotBlank(message = "Beneficiary account number is required")
    private String beneficiaryAccountNumber;

    private String bankName;

    private String nickname;
}
