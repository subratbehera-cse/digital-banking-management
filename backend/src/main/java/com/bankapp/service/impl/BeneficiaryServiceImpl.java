package com.bankapp.service.impl;

import com.bankapp.dto.request.CreateBeneficiaryRequest;
import com.bankapp.dto.response.BeneficiaryDto;
import com.bankapp.entity.Beneficiary;
import com.bankapp.entity.User;
import com.bankapp.exception.DuplicateResourceException;
import com.bankapp.exception.InvalidOperationException;
import com.bankapp.exception.ResourceNotFoundException;
import com.bankapp.mapper.BeneficiaryMapper;
import com.bankapp.repository.BankAccountRepository;
import com.bankapp.repository.BeneficiaryRepository;
import com.bankapp.service.BeneficiaryService;
import com.bankapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final BankAccountRepository accountRepository;
    private final UserService userService;

    @Override
    @Transactional
    public BeneficiaryDto addBeneficiary(Long userId, CreateBeneficiaryRequest request) {
        if (!accountRepository.existsByAccountNumber(request.getBeneficiaryAccountNumber())) {
            throw new InvalidOperationException(
                    "No bank account exists with number: " + request.getBeneficiaryAccountNumber());
        }
        if (beneficiaryRepository.existsByOwnerIdAndBeneficiaryAccountNumber(userId, request.getBeneficiaryAccountNumber())) {
            throw new DuplicateResourceException("This beneficiary has already been added");
        }

        User owner = userService.getUserEntityOrThrow(userId);

        Beneficiary beneficiary = Beneficiary.builder()
                .owner(owner)
                .beneficiaryName(request.getBeneficiaryName())
                .beneficiaryAccountNumber(request.getBeneficiaryAccountNumber())
                .bankName(request.getBankName())
                .nickname(request.getNickname())
                .build();

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary {} added for user id {}", saved.getBeneficiaryAccountNumber(), userId);
        return BeneficiaryMapper.toDto(saved);
    }

    @Override
    public List<BeneficiaryDto> getMyBeneficiaries(Long userId) {
        return beneficiaryRepository.findByOwnerId(userId).stream()
                .map(BeneficiaryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBeneficiary(Long userId, Long beneficiaryId) {
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwnerId(beneficiaryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        beneficiaryRepository.delete(beneficiary);
        log.info("Beneficiary id {} deleted by user id {}", beneficiaryId, userId);
    }
}
