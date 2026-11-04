package com.fundspark.fundspark_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fundspark.fundspark_backend.model.EquityCertificate;

public interface EquityCertificateRepository extends JpaRepository<EquityCertificate, Integer> {

    List<EquityCertificate> findByInvestorIdOrderByIssueDateDesc(Integer investorId);

    List<EquityCertificate> findByStartupId(Integer startupId);
}