package com.fundspark.fundspark_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Powers the "Ownership Dilution Chart" feature — rows here are written by
// InvestmentController.applyDilutionToExistingInvestors() every time a NEW
// investor comes into a startup. This controller only reads them back.
@RestController
@RequestMapping("/api/equity-history")
@CrossOrigin(origins = "*")
public class EquityHistoryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // GET /api/equity-history/investor/{investorId}/startup/{startupId}
    // Dilution timeline for ONE investor in ONE specific startup — feeds the
    // line chart on that investor's investment-details page.
    @GetMapping("/investor/{investorId}/startup/{startupId}")
    public List<Map<String, Object>> getForInvestorAndStartup(
            @PathVariable Integer investorId, @PathVariable Integer startupId) {
        String sql =
            "SELECT old_equity_percentage AS oldEquityPercentage, " +
            "new_equity_percentage AS newEquityPercentage, change_reason AS changeReason, " +
            "changed_at AS changedAt " +
            "FROM equity_history " +
            "WHERE investor_id = ? AND startup_id = ? " +
            "ORDER BY changed_at ASC";
        return jdbcTemplate.queryForList(sql, investorId, startupId);
    }

    // GET /api/equity-history/investor/{investorId}
    // Every dilution event across ALL of this investor's startups — enriched
    // with the startup name, newest first. Powers a general "equity activity" feed.
    @GetMapping("/investor/{investorId}")
    public List<Map<String, Object>> getForInvestor(@PathVariable Integer investorId) {
        String sql =
            "SELECT eh.startup_id AS startupId, s.name AS startupName, " +
            "eh.old_equity_percentage AS oldEquityPercentage, " +
            "eh.new_equity_percentage AS newEquityPercentage, eh.change_reason AS changeReason, " +
            "eh.changed_at AS changedAt " +
            "FROM equity_history eh " +
            "JOIN startups s ON s.startup_id = eh.startup_id " +
            "WHERE eh.investor_id = ? " +
            "ORDER BY eh.changed_at DESC";
        return jdbcTemplate.queryForList(sql, investorId);
    }
}