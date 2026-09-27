package com.bankapp.repository;

import com.bankapp.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByOwnerId(Long ownerId);
    Optional<Beneficiary> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByOwnerIdAndBeneficiaryAccountNumber(Long ownerId, String accountNumber);
}
