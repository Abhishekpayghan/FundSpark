package com.fundspark.fundspark_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.fundspark.fundspark_backend.model.InvestmentRequest;

import jakarta.persistence.LockModeType;

public interface InvestmentRequestRepository extends JpaRepository<InvestmentRequest, Integer> {

    // Latest request of one investor for one startup (newest first by id)
    Optional<InvestmentRequest> findFirstByInvestorIdAndStartupIdOrderByRequestIdDesc(Integer investorId, Integer startupId);

    // Used by the invest endpoint: finds the approved request and locks its row,
    // so two parallel clicks cannot both use the same approval.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InvestmentRequest> findFirstByInvestorIdAndStartupIdAndStatusOrderByRequestIdDesc(
            Integer investorId, Integer startupId, InvestmentRequest.Status status);
}