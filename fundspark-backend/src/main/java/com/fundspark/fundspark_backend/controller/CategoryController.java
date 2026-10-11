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

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    // GET /api/categories   -> powers Categories page + Explore filter sidebar
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // GET /api/categories/stats -> powers Admin Categories management with real DB counts
    @GetMapping("/stats")
    public List<java.util.Map<String, Object>> getCategoryStats() {
        String sql =
            "SELECT c.category_id AS id, c.name, c.icon, " +
            "       COUNT(s.startup_id) AS count, " +
            "       COALESCE(SUM(s.amount_raised), 0) AS totalRaised " +
            "FROM categories c " +
            "LEFT JOIN startups s ON s.category_id = c.category_id " +
            "GROUP BY c.category_id, c.name, c.icon " +
            "ORDER BY count DESC, c.name ASC";
        return jdbcTemplate.queryForList(sql);
    }

    // POST /api/categories -> Admin add new category
    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    // DELETE /api/categories/{id} -> Admin remove category
    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        if (!categoryRepository.existsById(id)) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        categoryRepository.deleteById(id);
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("message", "Category deleted"));
    }
}
