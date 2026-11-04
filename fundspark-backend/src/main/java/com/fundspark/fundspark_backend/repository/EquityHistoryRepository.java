package com.fundspark.fundspark_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fundspark.fundspark_backend.model.EquityHistory;

public interface EquityHistoryRepository extends JpaRepository<EquityHistory, Integer> {

    List<EquityHistory> findByInvestorIdAndStartupIdOrderByChangedAtAsc(Integer investorId, Integer startupId);

    List<EquityHistory> findByInvestorIdOrderByChangedAtAsc(Integer investorId);
}
