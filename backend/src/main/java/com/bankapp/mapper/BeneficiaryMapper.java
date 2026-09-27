package com.bankapp.mapper;

import com.bankapp.dto.response.BeneficiaryDto;
import com.bankapp.entity.Beneficiary;

public final class BeneficiaryMapper {

    private BeneficiaryMapper() {
    }

    public static BeneficiaryDto toDto(Beneficiary b) {
        return BeneficiaryDto.builder()
                .id(b.getId())
                .beneficiaryName(b.getBeneficiaryName())
                .beneficiaryAccountNumber(b.getBeneficiaryAccountNumber())
                .bankName(b.getBankName())
                .nickname(b.getNickname())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
