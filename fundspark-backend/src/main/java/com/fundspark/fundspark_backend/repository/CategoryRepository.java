package com.fundspark.fundspark_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fundspark.fundspark_backend.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
