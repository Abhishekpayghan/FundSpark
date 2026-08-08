package com.fundspark.fundspark_backend.controller;

import com.fundspark.fundspark_backend.model.Category;
import com.fundspark.fundspark_backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // GET /api/categories   -> powers Categories page + Explore filter sidebar
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
