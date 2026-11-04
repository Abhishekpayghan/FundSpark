package com.fundspark.fundspark_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fundspark.fundspark_backend.model.Startup;

public interface StartupRepository extends JpaRepository<Startup, Integer> {

    // Spring Data JPA auto-generates the SQL for these based on the method name —
    // no @Query needed for simple lookups like this.

    List<Startup> findByCategoryId(Integer categoryId);

    List<Startup> findByStatus(Startup.Status status);

    List<Startup> findByIsVerifiedTrue();

    List<Startup> findByFounderId(Integer founderId);
}
