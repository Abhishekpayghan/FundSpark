package com.fundspark.fundspark_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fundspark.fundspark_backend.model.InvestorBadge;

public interface InvestorBadgeRepository extends JpaRepository<InvestorBadge, Integer> {
    // Integer here is the investor_id (the PK) — no extra query methods needed yet
}
