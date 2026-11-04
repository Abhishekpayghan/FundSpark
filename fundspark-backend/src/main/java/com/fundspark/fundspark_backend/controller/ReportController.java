package com.fundspark.fundspark_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // GET /api/reports -> returns all reports with real joined data
    @GetMapping
    public List<Map<String, Object>> getAllReports() {
        String sql =
            "SELECT r.report_id AS id, " +
            "       COALESCE(s.name, u2.full_name, 'General Item') AS item, " +
            "       CASE WHEN r.reported_startup_id IS NOT NULL THEN 'Campaign' " +
            "            WHEN r.reported_user_id IS NOT NULL THEN 'User' " +
            "            ELSE 'General' END AS type, " +
            "       r.reason, " +
            "       COALESCE(u1.full_name, 'System') AS reportedBy, " +
            "       r.created_at AS date, " +
            "       r.status " +
            "FROM reports r " +
            "LEFT JOIN users u1 ON u1.user_id = r.reporter_id " +
            "LEFT JOIN startups s ON s.startup_id = r.reported_startup_id " +
            "LEFT JOIN users u2 ON u2.user_id = r.reported_user_id " +
            "ORDER BY r.created_at DESC";
        return jdbcTemplate.queryForList(sql);
    }

    // PUT /api/reports/{id}/dismiss
    @PutMapping("/{id}/dismiss")
    public ResponseEntity<?> dismissReport(@PathVariable Integer id) {
        jdbcTemplate.update("UPDATE reports SET status = 'dismissed' WHERE report_id = ?", id);
        return ResponseEntity.ok(Map.of("message", "Report dismissed"));
    }

    // PUT /api/reports/{id}/resolve
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Integer id) {
        jdbcTemplate.update("UPDATE reports SET status = 'reviewed' WHERE report_id = ?", id);
        return ResponseEntity.ok(Map.of("message", "Action taken on report"));
    }
}
