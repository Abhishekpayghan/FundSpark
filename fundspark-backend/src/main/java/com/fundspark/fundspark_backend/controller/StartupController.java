package com.fundspark.fundspark_backend.controller;

import com.fundspark.fundspark_backend.model.Startup;
import com.fundspark.fundspark_backend.repository.StartupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/startups")
@CrossOrigin(origins = "*")   // allows your frontend (running on Live Server / a different port) to call this API.
                              // Tighten this to your actual frontend URL before final submission if your professor cares about that.
public class StartupController {

    @Autowired
    private StartupRepository startupRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // GET /api/startups/feed
    // Powers the PUBLIC-facing pages: Landing, Explore, and Startup Details.
    // Both 'pending' and 'live' show up here — a new campaign is visible to everyone
    // right away, tagged as pending/unverified, and simply gains the Verified badge
    // once an admin approves it via /verify. Rejected/draft/closed campaigns stay hidden.
    @GetMapping("/feed")
    public List<Map<String, Object>> getFeed() {
        String sql =
            "SELECT s.startup_id AS id, s.name, u.full_name AS founder, c.name AS category, " +
            "s.tagline, s.funding_goal AS goal, s.amount_raised AS raised, s.location, s.status, " +
            "s.startup_score AS score, s.is_verified AS verified, s.is_trending AS trending, " +
            "s.valuation, s.founder_equity AS founderEquity, " +
            "DATEDIFF(s.deadline, CURDATE()) AS days " +
            "FROM startups s " +
            "JOIN users u ON u.user_id = s.founder_id " +
            "JOIN categories c ON c.category_id = s.category_id " +
            "WHERE s.status IN ('live','pending') " +
            "ORDER BY s.startup_id DESC";
        return jdbcTemplate.queryForList(sql);
    }

    // GET /api/startups/feed/{id}   -> single enriched startup, for the Startup Details page.
    // Deliberately NOT filtered by status — an admin viewing a closed/rejected startup via
    // the "View" icon, or a founder checking their own closed campaign, needs this to still
    // resolve. Discovery surfaces (Explore/Landing grids, via getFeed() above) are what stay
    // restricted to live/pending — this endpoint is "view a specific one if you have its id",
    // not "list what's discoverable".
    @GetMapping("/feed/{id}")
    public ResponseEntity<Map<String, Object>> getFeedItem(@PathVariable Integer id) {
        String sql =
            "SELECT s.startup_id AS id, s.name, u.full_name AS founder, c.name AS category, " +
            "s.tagline, s.description, s.funding_goal AS goal, s.amount_raised AS raised, s.location, s.status, " +
            "s.startup_score AS score, s.is_verified AS verified, s.is_trending AS trending, " +
            "s.valuation, s.founder_equity AS founderEquity, " +
            "DATEDIFF(s.deadline, CURDATE()) AS days " +
            "FROM startups s " +
            "JOIN users u ON u.user_id = s.founder_id " +
            "JOIN categories c ON c.category_id = s.category_id " +
            "WHERE s.startup_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);
        if (rows.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(rows.get(0));
    }

    // GET /api/startups/founder/{founderId}/feed
    // Powers the Founder "My Startups" / dashboard snapshot — a founder needs to see
    // ALL of their own campaigns (pending, live, rejected), not just the live ones.
    @GetMapping("/founder/{founderId}/feed")
    public List<Map<String, Object>> getFounderFeed(@PathVariable Integer founderId) {
        String sql =
            "SELECT s.startup_id AS id, s.name, c.name AS category, s.tagline, " +
            "s.funding_goal AS goal, s.amount_raised AS raised, s.status, s.is_verified AS verified, " +
            "DATEDIFF(s.deadline, CURDATE()) AS days " +
            "FROM startups s JOIN categories c ON c.category_id = s.category_id " +
            "WHERE s.founder_id = ? ORDER BY s.startup_id DESC";
        return jdbcTemplate.queryForList(sql, founderId);
    }

    // GET /api/startups/pending
    // Powers Admin > Campaign Approval — every startup awaiting review, enriched with
    // the real founder name and category name.
    @GetMapping("/pending")
    public List<Map<String, Object>> getPending() {
        String sql =
            "SELECT s.startup_id AS id, s.name, u.full_name AS founder, c.name AS category, " +
            "s.funding_goal AS goal, s.created_at AS createdAt, s.status " +
            "FROM startups s " +
            "JOIN users u ON u.user_id = s.founder_id " +
            "JOIN categories c ON c.category_id = s.category_id " +
            "WHERE s.status = 'pending' " +
            "ORDER BY s.created_at ASC";
        return jdbcTemplate.queryForList(sql);
    }

    // GET /api/startups/admin
    // Powers Admin > Startups management table — EVERY startup regardless of status,
    // enriched with founder/category names, for platform-wide moderation.
    @GetMapping("/admin")
    public List<Map<String, Object>> getAdminList() {
        String sql =
            "SELECT s.startup_id AS id, s.name, u.full_name AS founder, c.name AS category, " +
            "s.funding_goal AS goal, s.amount_raised AS raised, s.startup_score AS score, " +
            "s.is_verified AS verified, s.status, s.created_at AS createdAt " +
            "FROM startups s " +
            "JOIN users u ON u.user_id = s.founder_id " +
            "JOIN categories c ON c.category_id = s.category_id " +
            "ORDER BY s.startup_id DESC";
        return jdbcTemplate.queryForList(sql);
    }

    // GET /api/startups            -> raw entity list (kept for simple CRUD use)
    @GetMapping
    public List<Startup> getAllStartups() {
        return startupRepository.findAll();
    }

    // GET /api/startups/{id}       -> single RAW entity (all real DB fields, incl. deadline,
    // categoryId, founderId, status) — used by the Edit Startup form, which needs the actual
    // editable field values regardless of status, not the public /feed shape.
    @GetMapping("/{id}")
    public ResponseEntity<Startup> getStartupById(@PathVariable Integer id) {
        return startupRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/startups/category/{categoryId}   -> powers category filter on Explore
    @GetMapping("/category/{categoryId}")
    public List<Startup> getByCategory(@PathVariable Integer categoryId) {
        return startupRepository.findByCategoryId(categoryId);
    }

    // GET /api/startups/verified    -> only verified campaigns
    @GetMapping("/verified")
    public List<Startup> getVerifiedStartups() {
        return startupRepository.findByIsVerifiedTrue();
    }

    // POST /api/startups            -> powers Founder "Create Startup" form
    // Every new campaign starts 'pending' — invisible to the public feed until an admin
    // approves it. This is the "submit -> admin review -> goes live" flow in action.
    @PostMapping
    public Startup createStartup(@RequestBody Startup startup) {
        startup.setStatus(Startup.Status.pending);
        startup.setIsVerified(false);
        return startupRepository.save(startup);
    }

    // PUT /api/startups/{id}   -> powers the Founder "Edit Startup" form (Save Changes button).
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStartup(@PathVariable Integer id, @RequestBody Startup updates) {
        return startupRepository.findById(id).<ResponseEntity<?>>map(startup -> {
            if (updates.getName() != null) startup.setName(updates.getName());
            if (updates.getTagline() != null) startup.setTagline(updates.getTagline());
            if (updates.getDescription() != null) startup.setDescription(updates.getDescription());
            if (updates.getCategoryId() != null) startup.setCategoryId(updates.getCategoryId());
            if (updates.getFundingGoal() != null) startup.setFundingGoal(updates.getFundingGoal());
            if (updates.getDeadline() != null) startup.setDeadline(updates.getDeadline());
            if (updates.getLocation() != null) startup.setLocation(updates.getLocation());
            return ResponseEntity.ok(startupRepository.save(startup));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Startup not found"));
    }

    // PUT /api/startups/{id}/verify -> powers Admin "Approve" button.
    // This is the ONE action that makes a startup visible to the public /feed.
    @PutMapping("/{id}/verify")
    public ResponseEntity<Startup> verifyStartup(@PathVariable Integer id) {
        return startupRepository.findById(id).map(startup -> {
            startup.setIsVerified(true);
            startup.setStatus(Startup.Status.live);
            return ResponseEntity.ok(startupRepository.save(startup));
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/startups/{id}/reject -> powers Admin "Reject" button.
    @PutMapping("/{id}/reject")
    public ResponseEntity<Startup> rejectStartup(@PathVariable Integer id) {
        return startupRepository.findById(id).map(startup -> {
            startup.setStatus(Startup.Status.rejected);
            startup.setIsVerified(false);
            return ResponseEntity.ok(startupRepository.save(startup));
        }).orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/startups/{id}/close -> powers Founder "Close Campaign" AND Admin "Remove".
    // Both actions do the same underlying thing: status becomes 'closed', which the public
    // /feed queries exclude entirely (they only select 'live'/'pending') — so a closed
    // startup instantly disappears from Explore, Landing, and Startup Details for everyone.
    @PutMapping("/{id}/close")
    public ResponseEntity<Startup> closeStartup(@PathVariable Integer id) {
        return startupRepository.findById(id).map(startup -> {
            startup.setStatus(Startup.Status.closed);
            return ResponseEntity.ok(startupRepository.save(startup));
        }).orElse(ResponseEntity.notFound().build());
    }
}