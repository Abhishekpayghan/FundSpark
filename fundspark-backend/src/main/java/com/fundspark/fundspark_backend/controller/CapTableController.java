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

// Powers the "Live Cap Table" feature — reads from the v_cap_table VIEW
// created in the equity feature migration (Founder row + every investor's
// row, per startup).
@RestController
@RequestMapping("/api/cap-table")
@CrossOrigin(origins = "*")
public class CapTableController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // GET /api/cap-table/{startupId}
    // Returns every equity holder (Founder + each Investor) for one startup,
    // largest stake first — exactly what a pie chart / table needs.
    @GetMapping("/{startupId}")
    public List<Map<String, Object>> getCapTable(@PathVariable Integer startupId) {
        String sql =
            "SELECT holder_id, holder_name, holder_type, equity_percentage " +
            "FROM v_cap_table WHERE startup_id = ? " +
            "ORDER BY equity_percentage DESC";
        return jdbcTemplate.queryForList(sql, startupId);
    }

    // GET /api/cap-table/investor/{investorId}/portfolio
    // Returns every startup this investor holds equity in, and how much —
    // powers an investor's overall equity portfolio view.
    @GetMapping("/investor/{investorId}/portfolio")
    public List<Map<String, Object>> getInvestorPortfolio(@PathVariable Integer investorId) {
        String sql =
            "SELECT startup_id, startup_name, total_equity_percent, total_amount_invested " +
            "FROM v_investor_portfolio WHERE investor_id = ? " +
            "ORDER BY total_equity_percent DESC";
        return jdbcTemplate.queryForList(sql, investorId);
    }
}
