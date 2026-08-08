package com.fundspark.fundspark_backend.repository;

import com.fundspark.fundspark_backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
