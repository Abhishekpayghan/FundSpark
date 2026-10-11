package com.fundspark.fundspark_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fundspark.fundspark_backend.model.ValuationHistory;

public interface ValuationHistoryRepository extends JpaRepository<ValuationHistory, Integer> {

    List<ValuationHistory> findByStartupIdOrderByChangedAtAsc(Integer startupId);
}
