package com.fundspark.fundspark_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Powers the "Digital Equity Certificate" feature — certificates themselves
// are CREATED inside InvestmentController.invest() the moment an investment
// is confirmed. This controller only READS them back for display.
@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "*")
public class EquityCertificateController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // GET /api/certificates/investor/{investorId}
    // Powers the investor's "My Certificates" gallery — newest first, enriched
    // with the real startup name (not just its id).
    @GetMapping("/investor/{investorId}")
    public List<Map<String, Object>> getByInvestor(@PathVariable Integer investorId) {
        String sql =
            "SELECT ec.certificate_id AS id, ec.certificate_code AS certificateCode, " +
            "s.name AS startupName, ec.equity_percentage AS equityPercentage, " +
            "ec.amount_invested AS amountInvested, ec.issue_date AS issueDate " +
            "FROM equity_certificates ec " +
            "JOIN startups s ON s.startup_id = ec.startup_id " +
            "WHERE ec.investor_id = ? " +
            "ORDER BY ec.issue_date DESC";
        return jdbcTemplate.queryForList(sql, investorId);
    }

    // GET /api/certificates/startup/{startupId}
    // Powers a founder-side view — every certificate issued for THIS startup,
    // i.e. the list of everyone who has ever invested in it.
    @GetMapping("/startup/{startupId}")
    public List<Map<String, Object>> getByStartup(@PathVariable Integer startupId) {
        String sql =
            "SELECT ec.certificate_id AS id, ec.certificate_code AS certificateCode, " +
            "u.full_name AS investorName, ec.equity_percentage AS equityPercentage, " +
            "ec.amount_invested AS amountInvested, ec.issue_date AS issueDate " +
            "FROM equity_certificates ec " +
            "JOIN users u ON u.user_id = ec.investor_id " +
            "WHERE ec.startup_id = ? " +
            "ORDER BY ec.issue_date DESC";
        return jdbcTemplate.queryForList(sql, startupId);
    }

    // GET /api/certificates/{id}
    // Single certificate, fully enriched — used by the certificate detail/download view.
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Integer id) {
        String sql =
            "SELECT ec.certificate_id AS id, ec.certificate_code AS certificateCode, " +
            "u.full_name AS investorName, s.name AS startupName, " +
            "ec.equity_percentage AS equityPercentage, ec.amount_invested AS amountInvested, " +
            "ec.issue_date AS issueDate " +
            "FROM equity_certificates ec " +
            "JOIN users u ON u.user_id = ec.investor_id " +
            "JOIN startups s ON s.startup_id = ec.startup_id " +
            "WHERE ec.certificate_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);
        if (rows.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(rows.get(0));
    }
}