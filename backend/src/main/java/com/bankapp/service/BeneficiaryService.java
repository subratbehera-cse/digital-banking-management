package com.bankapp.service;

import com.bankapp.dto.request.CreateBeneficiaryRequest;
import com.bankapp.dto.response.BeneficiaryDto;

import java.util.List;

public interface BeneficiaryService {
    BeneficiaryDto addBeneficiary(Long userId, CreateBeneficiaryRequest request);
    List<BeneficiaryDto> getMyBeneficiaries(Long userId);
    void deleteBeneficiary(Long userId, Long beneficiaryId);
}
