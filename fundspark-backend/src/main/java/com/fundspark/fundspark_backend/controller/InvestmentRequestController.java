package com.fundspark.fundspark_backend.controller;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.fundspark.fundspark_backend.model.InvestmentRequest;
import com.fundspark.fundspark_backend.model.Notification;
import com.fundspark.fundspark_backend.model.Startup;
import com.fundspark.fundspark_backend.model.User;
import com.fundspark.fundspark_backend.repository.InvestmentRequestRepository;
import com.fundspark.fundspark_backend.repository.NotificationRepository;
import com.fundspark.fundspark_backend.repository.StartupRepository;
import com.fundspark.fundspark_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/investment-requests")
@CrossOrigin(origins = "*")
public class InvestmentRequestController {

    // An investor must wait this many hours before sending a new request for the same startup,
    // both when the founder did not respond and after a rejection.
    private static final int REQUEST_COOLDOWN_HOURS = 12;

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1000");

    // Allowed rejection reasons: code -> label shown to the investor
    private static final Map<String, String> REJECT_REASONS = new LinkedHashMap<>();
    static {
        REJECT_REASONS.put("INCOMPLETE_INFO", "The information provided is incomplete");
        REJECT_REASONS.put("AMOUNT_TOO_LOW", "The intended amount is too low");
        REJECT_REASONS.put("UNCLEAR_PURPOSE", "The investment purpose is not clear");
        REJECT_REASONS.put("NOT_ACCEPTING_INVESTORS", "The founder is not accepting new investors right now");
        REJECT_REASONS.put("OTHER", "Other");
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @Autowired
    private InvestmentRequestRepository requestRepository;

    @Autowired
    private StartupRepository startupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // GET /api/investment-requests/reject-reasons
    @GetMapping("/reject-reasons")
    public List<Map<String, String>> getRejectReasons() {
        List<Map<String, String>> out = new ArrayList<>();
        for (Map.Entry<String, String> e : REJECT_REASONS.entrySet()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", e.getKey());
            row.put("label", e.getValue());
            out.add(row);
        }
        return out;
    }

    // POST /api/investment-requests
    // Investor submits the "I'm Interested" form.
    @Transactional
    @PostMapping
    public ResponseEntity<?> create(@RequestBody InvestmentRequest body) {
        if (body.getInvestorId() == null || body.getStartupId() == null) {
            return error(HttpStatus.BAD_REQUEST, "Investor and startup are required.");
        }

        User investor = userRepository.findById(body.getInvestorId()).orElse(null);
        if (investor == null || investor.getRole() != User.Role.investor) {
            return error(HttpStatus.FORBIDDEN, "Only investor accounts can send investment requests.");
        }

        Startup startup = startupRepository.findById(body.getStartupId()).orElse(null);
        if (startup == null) {
            return error(HttpStatus.NOT_FOUND, "Startup not found.");
        }
        if (startup.getStatus() != Startup.Status.live) {
            return error(HttpStatus.BAD_REQUEST, "This campaign is not accepting investment requests.");
        }

        // ---- field validation ----
        if (body.getInvestorType() == null) {
            return error(HttpStatus.BAD_REQUEST, "Please select an investor type.");
        }
        if (body.getIntendedAmount() == null || body.getIntendedAmount().compareTo(MIN_AMOUNT) < 0) {
            return error(HttpStatus.BAD_REQUEST, "Intended amount must be at least 1000.");
        }
        String purpose = body.getPurpose() == null ? "" : body.getPurpose().trim();
        if (purpose.length() < 10 || purpose.length() > 500) {
            return error(HttpStatus.BAD_REQUEST, "Investment purpose must be between 10 and 500 characters.");
        }
        String experience = trimOrNull(body.getExperience());
        String message = trimOrNull(body.getMessageToFounder());
        if ((experience != null && experience.length() > 500) || (message != null && message.length() > 500)) {
            return error(HttpStatus.BAD_REQUEST, "Experience and message must be at most 500 characters each.");
        }

        // ---- duplicate / cooldown rules, based on the latest request ----
        Optional<InvestmentRequest> latest = requestRepository
                .findFirstByInvestorIdAndStartupIdOrderByRequestIdDesc(body.getInvestorId(), body.getStartupId());
        if (latest.isPresent()) {
            InvestmentRequest last = latest.get();

            if (last.getStatus() == InvestmentRequest.Status.accepted) {
                return error(HttpStatus.CONFLICT, "Your request is already approved. You can invest now.");
            }

            if (last.getStatus() == InvestmentRequest.Status.pending && !canRequestAgain(last)) {
                return error(HttpStatus.CONFLICT,
                        "Your request is still pending. If the founder does not respond, you can send a new request after "
                        + last.getCanReapplyAt().format(DATE_FMT) + ".");
            }
            if (last.getStatus() == InvestmentRequest.Status.rejected && !canRequestAgain(last)) {
                return error(HttpStatus.CONFLICT,
                        "You can send an updated request after " + last.getCanReapplyAt().format(DATE_FMT) + ".");
            }

            // The founder never answered and the wait is over: close the old request
            // so the founder sees only one open request from this investor.
            if (last.getStatus() == InvestmentRequest.Status.pending) {
                last.setStatus(InvestmentRequest.Status.expired);
                last.setDecidedAt(LocalDateTime.now());
                requestRepository.save(last);
            }
        }

        // ---- save a brand-new row (old requests are kept as history) ----
        InvestmentRequest req = new InvestmentRequest();
        req.setInvestorId(body.getInvestorId());
        req.setStartupId(body.getStartupId());
        req.setInvestorType(body.getInvestorType());
        req.setIntendedAmount(body.getIntendedAmount());
        req.setPurpose(purpose);
        req.setExperience(experience);
        req.setMessageToFounder(message);
        req.setStatus(InvestmentRequest.Status.pending);
        req.setCanReapplyAt(LocalDateTime.now().plusHours(REQUEST_COOLDOWN_HOURS));
        InvestmentRequest saved = requestRepository.save(req);

        notify(startup.getFounderId(), "user-plus", "New Investor Request",
                cut(investor.getFullName() + " wants to invest in " + startup.getName()
                        + ". Review the request in Investor Requests."));

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/investment-requests/status?investorId=1&startupId=2
    // Tells the startup page what the investor should see.
    @GetMapping("/status")
    public Map<String, Object> getStatus(@RequestParam Integer investorId, @RequestParam Integer startupId) {
        Map<String, Object> out = new LinkedHashMap<>();

        Optional<InvestmentRequest> latest = requestRepository
                .findFirstByInvestorIdAndStartupIdOrderByRequestIdDesc(investorId, startupId);

        if (latest.isEmpty()) {
            out.put("status", "none");
            out.put("canRequest", true);
            return out;
        }

        InvestmentRequest r = latest.get();
        out.put("requestId", r.getRequestId());
        out.put("status", r.getStatus().name());
        out.put("canRequest", canRequestAgain(r));

        if (r.getStatus() == InvestmentRequest.Status.pending || r.getStatus() == InvestmentRequest.Status.rejected) {
            out.put("canReapplyAt", r.getCanReapplyAt());
            out.put("hoursLeft", hoursLeft(r));
        }
        if (r.getStatus() == InvestmentRequest.Status.rejected) {
            out.put("rejectionReasonCode", r.getRejectionReason());
            out.put("rejectionReasonLabel", REJECT_REASONS.getOrDefault(r.getRejectionReason(), "Other"));
            out.put("rejectionMessage", r.getRejectionMessage());
        }
        return out;
    }

    // GET /api/investment-requests/investor/{investorId}
    @GetMapping("/investor/{investorId}")
    public List<Map<String, Object>> getByInvestor(@PathVariable Integer investorId) {
        String sql =
            "SELECT r.request_id AS id, r.startup_id AS startupId, s.name AS startupName, " +
            "r.investor_type AS investorType, r.intended_amount AS intendedAmount, r.purpose, r.experience, " +
            "r.message_to_founder AS messageToFounder, r.status, r.rejection_reason AS rejectionReason, " +
            "r.rejection_message AS rejectionMessage, r.created_at AS createdAt, r.decided_at AS decidedAt, " +
            "r.can_reapply_at AS canReapplyAt " +
            "FROM investment_requests r JOIN startups s ON s.startup_id = r.startup_id " +
            "WHERE r.investor_id = ? ORDER BY r.request_id DESC";
        return jdbcTemplate.queryForList(sql, investorId);
    }

    // GET /api/investment-requests/founder/{founderId}
    @GetMapping("/founder/{founderId}")
    public List<Map<String, Object>> getByFounder(@PathVariable Integer founderId) {
        String sql =
            "SELECT r.request_id AS id, r.startup_id AS startupId, s.name AS startupName, " +
            "u.user_id AS investorId, u.full_name AS investorName, u.avatar_initials AS investorInitials, " +
            "u.location AS investorLocation, r.investor_type AS investorType, " +
            "r.intended_amount AS intendedAmount, r.purpose, r.experience, r.message_to_founder AS messageToFounder, " +
            "r.status, r.rejection_reason AS rejectionReason, r.rejection_message AS rejectionMessage, " +
            "r.created_at AS createdAt, r.decided_at AS decidedAt, " +
            "(SELECT COUNT(*) FROM investment_requests p WHERE p.investor_id = r.investor_id " +
            "   AND p.startup_id = r.startup_id AND p.request_id < r.request_id) AS previousAttempts " +
            "FROM investment_requests r " +
            "JOIN startups s ON s.startup_id = r.startup_id " +
            "JOIN users u ON u.user_id = r.investor_id " +
            "WHERE s.founder_id = ? ORDER BY r.request_id DESC";
        return jdbcTemplate.queryForList(sql, founderId);
    }

    // PUT /api/investment-requests/{id}/accept?founderId=3
    @Transactional
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Integer id, @RequestParam Integer founderId) {
        InvestmentRequest req = requestRepository.findById(id).orElse(null);
        if (req == null) {
            return error(HttpStatus.NOT_FOUND, "Request not found.");
        }
        Startup startup = startupRepository.findById(req.getStartupId()).orElse(null);
        if (startup == null || !startup.getFounderId().equals(founderId)) {
            return error(HttpStatus.FORBIDDEN, "Only the founder of this startup can decide on this request.");
        }
        if (req.getStatus() != InvestmentRequest.Status.pending) {
            return error(HttpStatus.CONFLICT, "This request has already been decided or has expired.");
        }

        req.setStatus(InvestmentRequest.Status.accepted);
        req.setDecidedAt(LocalDateTime.now());
        req.setCanReapplyAt(null);
        requestRepository.save(req);

        notify(req.getInvestorId(), "check-circle", "Request Approved",
                cut("The founder of " + startup.getName() + " approved your request. You can invest now."));

        return ResponseEntity.ok(req);
    }

    // PUT /api/investment-requests/{id}/reject
    // Body: { "founderId": 3, "reasonCode": "AMOUNT_TOO_LOW", "message": "..." }
    // Both a reason code and a message (min 10 characters) are mandatory.
    @Transactional
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Integer founderId;
        try {
            founderId = Integer.valueOf(String.valueOf(body.get("founderId")));
        } catch (NumberFormatException ex) {
            return error(HttpStatus.BAD_REQUEST, "Founder id is required.");
        }

        String reasonCode = body.get("reasonCode") == null ? "" : String.valueOf(body.get("reasonCode")).trim();
        String message = body.get("message") == null ? "" : String.valueOf(body.get("message")).trim();

        if (!REJECT_REASONS.containsKey(reasonCode)) {
            return error(HttpStatus.BAD_REQUEST, "Please select a valid rejection reason.");
        }
        if (message.length() < 10 || message.length() > 500) {
            return error(HttpStatus.BAD_REQUEST,
                    "Please explain the rejection in 10 to 500 characters so the investor can fix it.");
        }

        InvestmentRequest req = requestRepository.findById(id).orElse(null);
        if (req == null) {
            return error(HttpStatus.NOT_FOUND, "Request not found.");
        }
        Startup startup = startupRepository.findById(req.getStartupId()).orElse(null);
        if (startup == null || !startup.getFounderId().equals(founderId)) {
            return error(HttpStatus.FORBIDDEN, "Only the founder of this startup can decide on this request.");
        }
        if (req.getStatus() != InvestmentRequest.Status.pending) {
            return error(HttpStatus.CONFLICT, "This request has already been decided or has expired.");
        }

        LocalDateTime now = LocalDateTime.now();
        req.setStatus(InvestmentRequest.Status.rejected);
        req.setRejectionReason(reasonCode);
        req.setRejectionMessage(message);
        req.setDecidedAt(now);
        req.setCanReapplyAt(now.plusHours(REQUEST_COOLDOWN_HOURS));
        requestRepository.save(req);

        notify(req.getInvestorId(), "x-circle", "Request Rejected",
                cut("Your request for " + startup.getName() + " was rejected: "
                        + REJECT_REASONS.get(reasonCode) + ". You can send an updated request in "
                        + REQUEST_COOLDOWN_HOURS + " hours."));

        return ResponseEntity.ok(req);
    }

    // GET /api/investment-requests/investor/{investorId}/verification-profile
    // Powers Founder "Verify Investor" profile modal:
    // Shows the investor's background, badge, and portfolio in OTHER startups.
    @GetMapping("/investor/{investorId}/verification-profile")
    public ResponseEntity<?> getInvestorVerificationProfile(@PathVariable Integer investorId) {
        User investor = userRepository.findById(investorId).orElse(null);
        if (investor == null) {
            return error(HttpStatus.NOT_FOUND, "Investor not found.");
        }

        Map<String, Object> out = new LinkedHashMap<>();

        // 1. Basic profile & contact information
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", investor.getUserId());
        profile.put("fullName", investor.getFullName());
        profile.put("email", investor.getEmail());
        profile.put("phone", investor.getPhone() != null ? investor.getPhone() : "Not provided");
        profile.put("location", investor.getLocation() != null ? investor.getLocation() : "Not provided");
        profile.put("bio", investor.getBio() != null ? investor.getBio() : "");
        profile.put("avatarInitials", investor.getAvatarInitials());
        profile.put("joinedAt", investor.getCreatedAt());
        out.put("investor", profile);

        // 2. Investor Tier Badge
        String badgeSql =
            "SELECT bt.tier_name AS tierName, bt.icon, bt.color_code AS colorCode, ib.total_invested AS totalInvested " +
            "FROM investor_badges ib " +
            "JOIN badge_tiers bt ON bt.tier_id = ib.tier_id " +
            "WHERE ib.investor_id = ?";
        List<Map<String, Object>> badgeRows = jdbcTemplate.queryForList(badgeSql, investorId);
        if (!badgeRows.isEmpty()) {
            out.put("badge", badgeRows.get(0));
        } else {
            Map<String, Object> fallbackBadge = new LinkedHashMap<>();
            fallbackBadge.put("tierName", "Bronze");
            fallbackBadge.put("icon", "award");
            fallbackBadge.put("colorCode", "#CD7F32");
            fallbackBadge.put("totalInvested", 0);
            out.put("badge", fallbackBadge);
        }

        // 3. Complete track record of investments in other startups
        String invSql =
            "SELECT i.investment_id AS investmentId, " +
            "       s.startup_id AS startupId, " +
            "       s.name AS startupName, " +
            "       c.name AS category, " +
            "       s.tagline AS startupTagline, " +
            "       s.location AS startupLocation, " +
            "       i.amount, " +
            "       i.equity_percentage AS equityPercentage, " +
            "       i.payment_method AS paymentMethod, " +
            "       i.status, " +
            "       i.invested_at AS investedAt " +
            "FROM investments i " +
            "JOIN startups s ON s.startup_id = i.startup_id " +
            "JOIN categories c ON c.category_id = s.category_id " +
            "WHERE i.investor_id = ? " +
            "ORDER BY i.invested_at DESC";
        List<Map<String, Object>> portfolio = jdbcTemplate.queryForList(invSql, investorId);
        out.put("portfolio", portfolio);

        // 4. Aggregated stats
        BigDecimal totalInvested = BigDecimal.ZERO;
        java.util.Set<Integer> distinctStartups = new java.util.HashSet<>();
        for (Map<String, Object> inv : portfolio) {
            if ("confirmed".equalsIgnoreCase(String.valueOf(inv.get("status")))) {
                Object amt = inv.get("amount");
                if (amt != null) {
                    totalInvested = totalInvested.add(new BigDecimal(amt.toString()));
                }
                Object sid = inv.get("startupId");
                if (sid != null) {
                    distinctStartups.add(Integer.valueOf(sid.toString()));
                }
            }
        }
        out.put("totalConfirmedInvested", totalInvested);
        out.put("startupsBackedCount", distinctStartups.size());
        out.put("totalInvestmentsCount", portfolio.size());

        return ResponseEntity.ok(out);
    }

    // ==================================================================
    // Helpers
    // ==================================================================

    // true when the investor may send a NEW request after this one
    private boolean canRequestAgain(InvestmentRequest r) {
        switch (r.getStatus()) {
            case accepted:
                return false;
            case pending:
            case rejected:
                return r.getCanReapplyAt() == null || !LocalDateTime.now().isBefore(r.getCanReapplyAt());
            case used:
            case expired:
            default:
                return true;
        }
    }

    // whole hours left in the wait (rounded up); 0 when it is over
    private int hoursLeft(InvestmentRequest r) {
        if (r.getCanReapplyAt() == null) return 0;
        long minutes = Duration.between(LocalDateTime.now(), r.getCanReapplyAt()).toMinutes();
        return minutes <= 0 ? 0 : (int) Math.ceil(minutes / 60.0);
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // notifications.message column is VARCHAR(255)
    private String cut(String s) {
        return s.length() <= 255 ? s : s.substring(0, 252) + "...";
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private void notify(Integer userId, String icon, String title, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setIcon(icon);
        n.setTitle(title);
        n.setMessage(message);
        n.setIsRead(false);
        notificationRepository.save(n);
    }
}