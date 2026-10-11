package com.fundspark.fundspark_backend.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @PersistenceContext
    private EntityManager entityManager;

    // GET /api/dashboard/founder/{founderId}
    // Powers the Founder Dashboard KPI cards: campaign count, total raised, investor count
    @GetMapping("/founder/{founderId}")
    public Map<String, Object> founderSummary(@PathVariable Integer founderId) {
        Map<String, Object> result = new HashMap<>();

        Query campaignCountQ = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM startups WHERE founder_id = :founderId");
        campaignCountQ.setParameter("founderId", founderId);
        result.put("activeCampaigns", ((Number) campaignCountQ.getSingleResult()).intValue());

        Query totalRaisedQ = entityManager.createNativeQuery(
            "SELECT COALESCE(SUM(amount_raised), 0) FROM startups WHERE founder_id = :founderId");
        totalRaisedQ.setParameter("founderId", founderId);
        result.put("totalRaised", totalRaisedQ.getSingleResult());

        Query investorCountQ = entityManager.createNativeQuery(
            "SELECT COUNT(DISTINCT i.investor_id) FROM investments i " +
            "JOIN startups s ON s.startup_id = i.startup_id WHERE s.founder_id = :founderId");
        investorCountQ.setParameter("founderId", founderId);
        result.put("investorCount", ((Number) investorCountQ.getSingleResult()).intValue());

        return result;
    }

    // GET /api/dashboard/investor/{investorId}
    // Powers the Investor Dashboard KPI cards: total invested, startups backed, wishlist count
    @GetMapping("/investor/{investorId}")
    public Map<String, Object> investorSummary(@PathVariable Integer investorId) {
        Map<String, Object> result = new HashMap<>();

        Query totalInvestedQ = entityManager.createNativeQuery(
            "SELECT COALESCE(SUM(amount), 0) FROM investments WHERE investor_id = :investorId AND status = 'confirmed'");
        totalInvestedQ.setParameter("investorId", investorId);
        result.put("totalInvested", totalInvestedQ.getSingleResult());

        Query backedCountQ = entityManager.createNativeQuery(
            "SELECT COUNT(DISTINCT startup_id) FROM investments WHERE investor_id = :investorId");
        backedCountQ.setParameter("investorId", investorId);
        result.put("startupsBacked", ((Number) backedCountQ.getSingleResult()).intValue());

        Query wishlistCountQ = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM wishlist WHERE investor_id = :investorId");
        wishlistCountQ.setParameter("investorId", investorId);
        result.put("wishlistCount", ((Number) wishlistCountQ.getSingleResult()).intValue());

        return result;
    }

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    // GET /api/dashboard/admin
    // Powers the Admin Dashboard platform-wide KPI cards
    @GetMapping("/admin")
    public Map<String, Object> adminSummary() {
        Map<String, Object> result = new HashMap<>();

        result.put("totalUsers", ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM users").getSingleResult()).intValue());

        result.put("totalFounders", ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM users WHERE role = 'founder'").getSingleResult()).intValue());

        result.put("totalInvestors", ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM users WHERE role = 'investor'").getSingleResult()).intValue());

        result.put("totalCampaigns", ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM startups").getSingleResult()).intValue());

        result.put("verifiedCampaigns", ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM startups WHERE is_verified = TRUE").getSingleResult()).intValue());

        result.put("pendingApprovals", ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM startups WHERE status = 'pending'").getSingleResult()).intValue());

        result.put("flaggedCampaigns", ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM reports WHERE status = 'pending'").getSingleResult()).intValue());

        Object totalRaised = entityManager.createNativeQuery(
            "SELECT COALESCE(SUM(amount_raised), 0) FROM startups").getSingleResult();
        result.put("totalFundingRaised", totalRaised);

        BigDecimal raisedBd = totalRaised != null ? new BigDecimal(totalRaised.toString()) : BigDecimal.ZERO;
        // Platform revenue: 2% of funds raised
        result.put("platformRevenue", raisedBd.multiply(new BigDecimal("0.02")));

        return result;
    }

    // GET /api/dashboard/admin/category-distribution
    // Powers the Admin "Category Distribution" doughnut chart — GROUP BY in action
    @SuppressWarnings("unchecked")
    @GetMapping("/admin/category-distribution")
    public List<Object[]> categoryDistribution() {
        Query q = entityManager.createNativeQuery(
            "SELECT c.name, COUNT(s.startup_id) as total " +
            "FROM categories c LEFT JOIN startups s ON s.category_id = c.category_id " +
            "GROUP BY c.category_id, c.name ORDER BY total DESC");
        return q.getResultList();
    }

    // GET /api/dashboard/admin/analytics
    // Powers Admin Analytics page and Dashboard charts with 100% REAL data from MySQL
    @GetMapping("/admin/analytics")
    public Map<String, Object> adminAnalytics() {
        Map<String, Object> data = new java.util.LinkedHashMap<>();

        // 1. Founders vs Investors count
        int founders = ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM users WHERE role = 'founder'").getSingleResult()).intValue();
        int investors = ((Number) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM users WHERE role = 'investor'").getSingleResult()).intValue();
        data.put("foundersCount", founders);
        data.put("investorsCount", investors);

        // 2. User Growth (Real monthly user registrations)
        String userGrowthSql =
            "SELECT DATE_FORMAT(created_at, '%b %Y') AS monthLabel, " +
            "       DATE_FORMAT(created_at, '%Y-%m') AS ym, " +
            "       COUNT(*) AS userCount " +
            "FROM users " +
            "GROUP BY ym, monthLabel " +
            "ORDER BY ym ASC";
        List<Map<String, Object>> userGrowthRows = jdbcTemplate.queryForList(userGrowthSql);
        data.put("userGrowth", userGrowthRows);

        // 3. Monthly Funding Volume (Real confirmed investments aggregated by month)
        String fundingSql =
            "SELECT DATE_FORMAT(invested_at, '%b %Y') AS monthLabel, " +
            "       DATE_FORMAT(invested_at, '%Y-%m') AS ym, " +
            "       COALESCE(SUM(amount), 0) AS totalAmount " +
            "FROM investments " +
            "WHERE status = 'confirmed' " +
            "GROUP BY ym, monthLabel " +
            "ORDER BY ym ASC";
        List<Map<String, Object>> fundingRows = jdbcTemplate.queryForList(fundingSql);
        data.put("monthlyFunding", fundingRows);

        // 4. Category Distribution (Real counts of startups per category)
        String catDistSql =
            "SELECT c.name AS category, COUNT(s.startup_id) AS total " +
            "FROM categories c " +
            "LEFT JOIN startups s ON s.category_id = c.category_id " +
            "GROUP BY c.category_id, c.name " +
            "HAVING total > 0 " +
            "ORDER BY total DESC";
        List<Map<String, Object>> catDistRows = jdbcTemplate.queryForList(catDistSql);
        if (catDistRows.isEmpty()) {
            catDistRows = jdbcTemplate.queryForList(
                "SELECT c.name AS category, 0 AS total FROM categories c LIMIT 6");
        }
        data.put("categoryDistribution", catDistRows);

        // 5. Top Performing Categories (Real data for Top Performing Categories table)
        String topCatSql =
            "SELECT c.name AS category, " +
            "       COUNT(s.startup_id) AS campaigns, " +
            "       COALESCE(SUM(s.amount_raised), 0) AS totalRaised, " +
            "       CASE WHEN COUNT(s.startup_id) = 0 THEN 0 " +
            "            ELSE ROUND(SUM(CASE WHEN s.amount_raised >= s.funding_goal THEN 1 ELSE 0 END) * 100.0 / COUNT(s.startup_id), 0) " +
            "       END AS successRate " +
            "FROM categories c " +
            "JOIN startups s ON s.category_id = c.category_id " +
            "GROUP BY c.category_id, c.name " +
            "ORDER BY totalRaised DESC, campaigns DESC";
        List<Map<String, Object>> topCats = jdbcTemplate.queryForList(topCatSql);
        data.put("topCategories", topCats);

        return data;
    }
}
