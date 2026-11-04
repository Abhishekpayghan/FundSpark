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

// Powers the "Investor Tier Badges" feature — badges themselves are kept in
// sync inside InvestmentController.updateInvestorBadge() after every
// confirmed investment. This controller only reads them back.
@RestController
@RequestMapping("/api/badges")
@CrossOrigin(origins = "*")
public class InvestorBadgeController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // GET /api/badges/tiers
    // The 4 reference tiers (Bronze/Silver/Gold/Platinum) with their thresholds —
    // used by the frontend to show "₹X more to reach the next tier".
    @GetMapping("/tiers")
    public List<Map<String, Object>> getTiers() {
        String sql = "SELECT tier_id AS tierId, tier_name AS tierName, min_invested AS minInvested, " +
                "icon, color FROM badge_tiers ORDER BY min_invested ASC";
        return jdbcTemplate.queryForList(sql);
    }

    // GET /api/badges/investor/{investorId}
    // One investor's current badge, enriched with tier name/icon/color and how much
    // more they need to invest to reach the NEXT tier (nextTierName/amountToNextTier
    // come back null when they're already at the top tier, Platinum).
    @GetMapping("/investor/{investorId}")
    public ResponseEntity<Map<String, Object>> getForInvestor(@PathVariable Integer investorId) {
        String sql =
            "SELECT ib.investor_id AS investorId, ib.total_invested AS totalInvested, " +
            "bt.tier_name AS tierName, bt.icon, bt.color, " +
            "(SELECT MIN(min_invested) FROM badge_tiers WHERE min_invested > ib.total_invested) AS nextTierThreshold, " +
            "(SELECT tier_name FROM badge_tiers WHERE min_invested > ib.total_invested ORDER BY min_invested ASC LIMIT 1) AS nextTierName " +
            "FROM investor_badges ib " +
            "JOIN badge_tiers bt ON bt.tier_id = ib.tier_id " +
            "WHERE ib.investor_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, investorId);
        if (rows.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(rows.get(0));
    }

    // GET /api/badges/leaderboard
    // Top investors by total confirmed investment amount — powers the "Investor
    // Leaderboard" page. Limited to top 50 so the page stays fast.
    @GetMapping("/leaderboard")
    public List<Map<String, Object>> getLeaderboard() {
        String sql =
            "SELECT u.full_name AS investorName, ib.total_invested AS totalInvested, " +
            "bt.tier_name AS tierName, bt.icon, bt.color " +
            "FROM investor_badges ib " +
            "JOIN users u ON u.user_id = ib.investor_id " +
            "JOIN badge_tiers bt ON bt.tier_id = ib.tier_id " +
            "ORDER BY ib.total_invested DESC " +
            "LIMIT 50";
        return jdbcTemplate.queryForList(sql);
    }
}