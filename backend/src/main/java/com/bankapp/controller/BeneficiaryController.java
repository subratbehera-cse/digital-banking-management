package com.bankapp.controller;

import com.bankapp.dto.request.CreateBeneficiaryRequest;
import com.bankapp.dto.response.ApiResponse;
import com.bankapp.dto.response.BeneficiaryDto;
import com.bankapp.security.UserPrincipal;
import com.bankapp.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryDto>> add(@AuthenticationPrincipal UserPrincipal principal,
                                                             @Valid @RequestBody CreateBeneficiaryRequest request) {
        BeneficiaryDto dto = beneficiaryService.addBeneficiary(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Beneficiary added", dto));
    }

    @GetMapping
    public ApiResponse<List<BeneficiaryDto>> getMine(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(beneficiaryService.getMyBeneficiaries(principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        beneficiaryService.deleteBeneficiary(principal.getId(), id);
        return ApiResponse.success("Beneficiary removed", null);
    }
}
