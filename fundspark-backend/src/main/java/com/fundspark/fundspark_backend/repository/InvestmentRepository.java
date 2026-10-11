package com.fundspark.fundspark_backend.repository;

import com.fundspark.fundspark_backend.model.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Integer> {

    List<Investment> findByStartupId(Integer startupId);

    List<Investment> findByInvestorId(Integer investorId);
}
