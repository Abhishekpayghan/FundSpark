package com.fundspark.fundspark_backend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Account deletion approval flow.
 *   User  -> POST /api/deletion-requests                (asks admin for permission)
 *   User  -> GET  /api/deletion-requests/user/{userId}  (checks the status of the latest request)
 *   Admin -> GET  /api/deletion-requests?status=pending (review list with impact summary)
 *   Admin -> PUT  /api/deletion-requests/{id}/decision  (approve or reject)
 * The final delete is DELETE /api/users/{id} (UserController), which only works after approval.
 */
@RestController
@RequestMapping("/api/deletion-requests")
@CrossOrigin(origins = "*")
public class AccountDeletionController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private void notifyUser(Integer userId, String icon, String title, String message) {
        jdbcTemplate.update(
            "INSERT INTO notifications (user_id, icon, title, message, is_read) VALUES (?, ?, ?, ?, FALSE)",
            userId, icon, title, message);
    }

    // What would be lost if this account were removed — shown to the admin before deciding.
    private Map<String, Object> buildImpact(Integer userId, String role) {
        Map<String, Object> impact = new LinkedHashMap<>();
        if ("founder".equals(role)) {
            Map<String, Object> s = jdbcTemplate.queryForMap(
                "SELECT COUNT(*) AS total_startups, " +
                "COALESCE(SUM(CASE WHEN status = 'live' THEN 1 ELSE 0 END), 0) AS live_startups, " +
                "COALESCE(SUM(amount_raised), 0) AS total_raised " +
                "FROM startups WHERE founder_id = ?", userId);
            Integer investors = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT i.investor_id) FROM investments i " +
                "JOIN startups s ON s.startup_id = i.startup_id " +
                "WHERE s.founder_id = ? AND i.status <> 'failed'", Integer.class, userId);
            impact.put("totalStartups", s.get("total_startups"));
            impact.put("liveStartups", s.get("live_startups"));
            impact.put("totalRaised", s.get("total_raised"));
            impact.put("investorCount", investors == null ? 0 : investors);
        } else {
            Map<String, Object> s = jdbcTemplate.queryForMap(
                "SELECT COUNT(*) AS investment_count, COALESCE(SUM(amount), 0) AS total_invested " +
                "FROM investments WHERE investor_id = ? AND status <> 'failed'", userId);
            impact.put("investmentCount", s.get("investment_count"));
            impact.put("totalInvested", s.get("total_invested"));
        }
        return impact;
    }

    // POST /api/deletion-requests   body: { userId, reason }
    @PostMapping
    @Transactional
    public ResponseEntity<?> createRequest(@RequestBody Map<String, Object> body) {
        Object uid = body.get("userId");
        String reason = body.get("reason") == null ? "" : String.valueOf(body.get("reason")).trim();
        if (uid == null) {
            return error(HttpStatus.BAD_REQUEST, "User id is required.");
        }
        Integer userId = Integer.valueOf(String.valueOf(uid));
        if (reason.length() < 10 || reason.length() > 500) {
            return error(HttpStatus.BAD_REQUEST, "Please give a reason between 10 and 500 characters.");
        }

        List<Map<String, Object>> users = jdbcTemplate.queryForList(
            "SELECT full_name, role FROM users WHERE user_id = ?", userId);
        if (users.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "User not found.");
        }
        String role = String.valueOf(users.get(0).get("role"));
        String name = String.valueOf(users.get(0).get("full_name"));
        if ("admin".equals(role)) {
            return error(HttpStatus.FORBIDDEN, "Admin accounts cannot be deleted from here.");
        }

        List<String> open = jdbcTemplate.queryForList(
            "SELECT status FROM account_deletion_requests WHERE user_id = ? AND status IN ('pending','approved')",
            String.class, userId);
        if (open.contains("approved")) {
            return error(HttpStatus.CONFLICT, "Your request is already approved. You can delete your account now.");
        }
        if (open.contains("pending")) {
            return error(HttpStatus.CONFLICT, "You already have a request waiting for admin review.");
        }

        jdbcTemplate.update(
            "INSERT INTO account_deletion_requests (user_id, reason, status) VALUES (?, ?, 'pending')",
            userId, reason);

        // Tell every admin that a new request is waiting
        List<Integer> admins = jdbcTemplate.queryForList(
            "SELECT user_id FROM users WHERE role = 'admin'", Integer.class);
        for (Integer adminId : admins) {
            notifyUser(adminId, "user-x", "Account deletion request",
                name + " (" + role + ") has asked to delete their account.");
        }

        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("message", "Request sent. An admin will review it.");
        return ResponseEntity.status(HttpStatus.CREATED).body(ok);
    }

    // GET /api/deletion-requests/user/{userId}   -> latest request of this user (204 if none)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> latestForUser(@PathVariable Integer userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT request_id AS id, user_id AS userId, reason, status, admin_note AS adminNote, " +
            "created_at AS createdAt, decided_at AS decidedAt " +
            "FROM account_deletion_requests WHERE user_id = ? ORDER BY request_id DESC LIMIT 1", userId);
        if (rows.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rows.get(0));
    }

    // GET /api/deletion-requests?status=pending|approved|rejected|all   (admin)
    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(defaultValue = "pending") String status) {
        String sql =
            "SELECT r.request_id AS id, r.user_id AS userId, r.reason, r.status, r.admin_note AS adminNote, " +
            "r.created_at AS createdAt, r.decided_at AS decidedAt, " +
            "u.full_name AS fullName, u.email, u.role " +
            "FROM account_deletion_requests r JOIN users u ON u.user_id = r.user_id ";
        List<Map<String, Object>> rows;
        if ("all".equals(status)) {
            rows = jdbcTemplate.queryForList(sql + "ORDER BY r.request_id DESC");
        } else {
            rows = jdbcTemplate.queryForList(sql + "WHERE r.status = ? ORDER BY r.request_id DESC", status);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> row = new LinkedHashMap<>(r);
            row.put("impact", buildImpact((Integer) r.get("userId"), String.valueOf(r.get("role"))));
            out.add(row);
        }
        return out;
    }

    // GET /api/deletion-requests/pending-count   (admin sidebar badge)
    @GetMapping("/pending-count")
    public Map<String, Object> pendingCount() {
        Integer c = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM account_deletion_requests WHERE status = 'pending'", Integer.class);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("count", c == null ? 0 : c);
        return m;
    }

    // PUT /api/deletion-requests/{id}/decision   body: { decision: "approved"|"rejected", adminNote }
    @PutMapping("/{id}/decision")
    @Transactional
    public ResponseEntity<?> decide(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String decision = body.get("decision");
        String note = body.get("adminNote") == null ? "" : body.get("adminNote").trim();
        if (!"approved".equals(decision) && !"rejected".equals(decision)) {
            return error(HttpStatus.BAD_REQUEST, "Decision must be 'approved' or 'rejected'.");
        }
        if ("rejected".equals(decision) && note.length() < 5) {
            return error(HttpStatus.BAD_REQUEST, "Please write a short reason when rejecting.");
        }
        if (note.length() > 500) {
            return error(HttpStatus.BAD_REQUEST, "Note must be 500 characters or fewer.");
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT user_id, status FROM account_deletion_requests WHERE request_id = ?", id);
        if (rows.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Request not found.");
        }
        if (!"pending".equals(String.valueOf(rows.get(0).get("status")))) {
            return error(HttpStatus.CONFLICT, "This request has already been decided.");
        }
        Integer userId = (Integer) rows.get(0).get("user_id");

        jdbcTemplate.update(
            "UPDATE account_deletion_requests SET status = ?, admin_note = ?, decided_at = NOW() WHERE request_id = ?",
            decision, note.isEmpty() ? null : note, id);

        if ("approved".equals(decision)) {
            notifyUser(userId, "check-circle", "Account deletion approved",
                "An admin approved your request. Go to Settings and confirm to delete your account.");
        } else {
            notifyUser(userId, "x-circle", "Account deletion rejected",
                "An admin rejected your request. Reason: " + note);
        }

        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("message", "Request " + decision + ".");
        return ResponseEntity.ok(ok);
    }
}