package com.fundspark.fundspark_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fundspark.fundspark_backend.model.BadgeTier;

public interface BadgeTierRepository extends JpaRepository<BadgeTier, Integer> {

    List<BadgeTier> findAllByOrderByMinInvestedAsc();
}
