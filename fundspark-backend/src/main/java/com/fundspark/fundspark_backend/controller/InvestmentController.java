package com.fundspark.fundspark_backend.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fundspark.fundspark_backend.model.BadgeTier;
import com.fundspark.fundspark_backend.model.EquityCertificate;
import com.fundspark.fundspark_backend.model.EquityHistory;
import com.fundspark.fundspark_backend.model.Investment;
import com.fundspark.fundspark_backend.model.InvestmentRequest;
import com.fundspark.fundspark_backend.model.InvestorBadge;
import com.fundspark.fundspark_backend.model.Notification;
import com.fundspark.fundspark_backend.model.Startup;
import com.fundspark.fundspark_backend.model.ValuationHistory;
import com.fundspark.fundspark_backend.repository.BadgeTierRepository;
import com.fundspark.fundspark_backend.repository.EquityCertificateRepository;
import com.fundspark.fundspark_backend.repository.EquityHistoryRepository;
import com.fundspark.fundspark_backend.repository.InvestmentRepository;
import com.fundspark.fundspark_backend.repository.InvestmentRequestRepository;
import com.fundspark.fundspark_backend.repository.InvestorBadgeRepository;
import com.fundspark.fundspark_backend.repository.NotificationRepository;
import com.fundspark.fundspark_backend.repository.StartupRepository;
import com.fundspark.fundspark_backend.repository.ValuationHistoryRepository;

@RestController
@RequestMapping("/api/investments")
@CrossOrigin(origins = "*")
public class InvestmentController {

    // Working precision for intermediate division (avoids "non-terminating
    // decimal expansion" errors); final values are rounded down to the
    // column's real scale (3 for %, 2 for money) before saving.
    private static final int CALC_SCALE = 10;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private InvestmentRequestRepository investmentRequestRepository;

    @Autowired
    private StartupRepository startupRepository;

    @Autowired
    private EquityCertificateRepository equityCertificateRepository;

    @Autowired
    private EquityHistoryRepository equityHistoryRepository;

    @Autowired
    private ValuationHistoryRepository valuationHistoryRepository;

    @Autowired
    private InvestorBadgeRepository investorBadgeRepository;

    @Autowired
    private BadgeTierRepository badgeTierRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // GET /api/investments/startup/{startupId}   -> raw rows (kept for simple use)
    @GetMapping("/startup/{startupId}")
    public List<Investment> getByStartup(@PathVariable Integer startupId) {
        return investmentRepository.findByStartupId(startupId);
    }

    // GET /api/investments/investor/{investorId}   -> raw rows (kept for simple use)
    @GetMapping("/investor/{investorId}")
    public List<Investment> getByInvestor(@PathVariable Integer investorId) {
        return investmentRepository.findByInvestorId(investorId);
    }

    // GET /api/investments/founder/{founderId}/feed
    @GetMapping("/founder/{founderId}/feed")
    public List<Map<String, Object>> getByFounderFeed(@PathVariable Integer founderId) {
        String sql =
            "SELECT i.investment_id AS id, u.full_name AS investorName, u.avatar_initials AS investorInitials, " +
            "s.name AS startupName, i.amount, i.equity_percentage AS equityPercentage, i.status, i.invested_at AS investedAt " +
            "FROM investments i " +
            "JOIN users u ON u.user_id = i.investor_id " +
            "JOIN startups s ON s.startup_id = i.startup_id " +
            "WHERE s.founder_id = ? ORDER BY i.invested_at DESC";
        return jdbcTemplate.queryForList(sql, founderId);
    }

    // GET /api/investments/investor/{investorId}/feed
    @GetMapping("/investor/{investorId}/feed")
    public List<Map<String, Object>> getByInvestorFeed(@PathVariable Integer investorId) {
        String sql =
            "SELECT i.investment_id AS id, s.startup_id AS startupId, s.name AS startupName, " +
            "c.name AS category, i.amount, i.equity_percentage AS equityPercentage, i.status, i.invested_at AS investedAt " +
            "FROM investments i " +
            "JOIN startups s ON s.startup_id = i.startup_id " +
            "JOIN categories c ON c.category_id = s.category_id " +
            "WHERE i.investor_id = ? ORDER BY i.invested_at DESC";
        return jdbcTemplate.queryForList(sql, investorId);
    }

    // POST /api/investments   -> powers the "Invest Now" modal on Startup Details
    //
    // On top of the original save + amount_raised update, this now also:
    //   1. Calculates the equity % this investment buys (amount / valuation * 100)
    //   2. Dilutes the founder AND every existing investor proportionally
    //   3. Logs each affected investor's old->new % in equity_history
    //   4. Issues a Digital Equity Certificate for the new investment
    //   5. Updates the investor's badge tier (Bronze/Silver/Gold/Platinum)
    //   6. Sends notifications for dilution, the new certificate, and badge upgrades
    //
    // @Transactional: if ANY step fails, EVERYTHING rolls back — no half-updated
    // cap table, no orphan certificate, etc.
    @Transactional
    @PostMapping
    public ResponseEntity<?> invest(@RequestBody Investment investment) {
        Startup startup = startupRepository.findById(investment.getStartupId())
                .orElse(null);

               if (startup == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Startup not found");
        }

        if (investment.getInvestorId() == null || investment.getAmount() == null
                || investment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Investor and a valid amount are required."));
        }

        // Founder approval gate: the investor needs an accepted request for this startup
        InvestmentRequest approval = investmentRequestRepository
                .findFirstByInvestorIdAndStartupIdAndStatusOrderByRequestIdDesc(
                        investment.getInvestorId(), investment.getStartupId(), InvestmentRequest.Status.accepted)
                .orElse(null);
        if (approval == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Founder approval is required before investing. Please send an investment request first."));
        }

        ensureValuation(startup);

        BigDecimal amount = investment.getAmount();

        // fraction of the company this investment buys, e.g. 0.02 for 2%
        BigDecimal fraction = amount.divide(startup.getValuation(), CALC_SCALE, RoundingMode.HALF_UP);
        BigDecimal newInvestorEquityPct = fraction.multiply(BigDecimal.valueOf(100))
                .setScale(3, RoundingMode.HALF_UP);

        // Simulated payment: mark confirmed immediately (no real gateway in this project)
        investment.setStatus(Investment.Status.confirmed);
        investment.setEquityPercentage(newInvestorEquityPct);
        Investment saved = investmentRepository.save(investment);

        // Dilute everyone who already held equity BEFORE this investment
        BigDecimal dilutionFactor = BigDecimal.ONE.subtract(fraction); // e.g. 0.98
        applyDilutionToExistingInvestors(startup, saved.getInvestmentId(), dilutionFactor);

        // Founder's slice shrinks by the same factor
        startup.setFounderEquity(startup.getFounderEquity()
                .multiply(dilutionFactor).setScale(2, RoundingMode.HALF_UP));

        // Update the startup's running total raised
        startup.setAmountRaised(startup.getAmountRaised().add(amount));
        startupRepository.save(startup);

        // Issue the digital certificate for this investment
        issueCertificate(saved, startup, newInvestorEquityPct, amount);

        // Update the investor's cumulative total + badge tier
        updateInvestorBadge(investment.getInvestorId());

        // Let the new investor know their investment + equity + certificate is ready
        notify(investment.getInvestorId(), "award", "Investment Confirmed",
                "You received " + newInvestorEquityPct + "% equity in " + startup.getName() + ". Your certificate is ready.");
                      // One approval allows one investment only: close it now
        approval.setStatus(InvestmentRequest.Status.used);
        investmentRequestRepository.save(approval);  

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ------------------------------------------------------------------
    // Helper: if a startup has no valuation yet (e.g. created after this
    // feature was added, before the founder explicitly set one), default
    // it to 10x the funding goal and log it as the "Initial" round —
    // same rule used to backfill old startups in the DB migration.
    // ------------------------------------------------------------------
    private void ensureValuation(Startup startup) {
        if (startup.getValuation() == null) {
            BigDecimal defaultValuation = startup.getFundingGoal().multiply(BigDecimal.TEN);
            startup.setValuation(defaultValuation);
            startupRepository.save(startup);

            ValuationHistory vh = new ValuationHistory();
            vh.setStartupId(startup.getStartupId());
            vh.setRoundName("Initial");
            vh.setValuationAmount(defaultValuation);
            valuationHistoryRepository.save(vh);
        }
    }

    // ------------------------------------------------------------------
    // Helper: shrink every EXISTING investor's total equity in this startup
    // by dilutionFactor, log ONE equity_history row per affected investor,
    // and notify them their stake changed.
    // ------------------------------------------------------------------
    private void applyDilutionToExistingInvestors(Startup startup, Integer newInvestmentId, BigDecimal dilutionFactor) {
        List<Investment> priorInvestments = investmentRepository.findByStartupId(startup.getStartupId());

        // Group prior CONFIRMED investments (excluding the one we just created) by investor
        Map<Integer, BigDecimal> oldTotalByInvestor = new java.util.HashMap<>();
        Map<Integer, List<Investment>> rowsByInvestor = new java.util.HashMap<>();

        for (Investment inv : priorInvestments) {
            if (inv.getInvestmentId().equals(newInvestmentId)) continue; // skip the brand-new one
            if (inv.getStatus() != Investment.Status.confirmed) continue;

            oldTotalByInvestor.merge(inv.getInvestorId(), inv.getEquityPercentage(), BigDecimal::add);
            rowsByInvestor.computeIfAbsent(inv.getInvestorId(), k -> new java.util.ArrayList<>()).add(inv);
        }

        for (Map.Entry<Integer, List<Investment>> entry : rowsByInvestor.entrySet()) {
            Integer investorId = entry.getKey();
            BigDecimal oldTotal = oldTotalByInvestor.get(investorId);

            // Shrink each of that investor's individual investment rows by the same factor,
            // so the cap table (which SUMs these) always stays accurate.
            BigDecimal newTotal = BigDecimal.ZERO;
            for (Investment row : entry.getValue()) {
                BigDecimal newPct = row.getEquityPercentage().multiply(dilutionFactor).setScale(3, RoundingMode.HALF_UP);
                row.setEquityPercentage(newPct);
                investmentRepository.save(row);
                newTotal = newTotal.add(newPct);
            }

            EquityHistory history = new EquityHistory();
            history.setInvestorId(investorId);
            history.setStartupId(startup.getStartupId());
            history.setOldEquityPercentage(oldTotal);
            history.setNewEquityPercentage(newTotal);
            history.setChangeReason("New investment received by " + startup.getName());
            equityHistoryRepository.save(history);

            notify(investorId, "trending-down", "Equity Diluted",
                    "Due to a new investment in " + startup.getName() + ", your stake changed from " + oldTotal + "% to " + newTotal + "%.");
        }
    }

    // ------------------------------------------------------------------
    // Helper: create the Digital Equity Certificate row for a confirmed investment.
    // certificate_code is left null here — the DB trigger (trg_certificate_code)
    // fills it automatically (e.g. FS-CERT-000001).
    // ------------------------------------------------------------------
    private void issueCertificate(Investment saved, Startup startup, BigDecimal equityPct, BigDecimal amount) {
        EquityCertificate cert = new EquityCertificate();
        cert.setInvestmentId(saved.getInvestmentId());
        cert.setInvestorId(saved.getInvestorId());
        cert.setStartupId(startup.getStartupId());
        cert.setEquityPercentage(equityPct);
        cert.setAmountInvested(amount);
        equityCertificateRepository.save(cert);
    }

    // ------------------------------------------------------------------
    // Helper: recompute an investor's total confirmed investment amount,
    // find their correct badge tier, save/update investor_badges, and
    // notify them if they just leveled up.
    // ------------------------------------------------------------------
    private void updateInvestorBadge(Integer investorId) {
        BigDecimal total = jdbcTemplate.queryForObject(
                "SELECT IFNULL(SUM(amount), 0) FROM investments WHERE investor_id = ? AND status = 'confirmed'",
                BigDecimal.class, investorId);

        List<BadgeTier> tiers = badgeTierRepository.findAllByOrderByMinInvestedAsc();
        BadgeTier newTier = tiers.get(0); // Bronze is always the fallback (min_invested = 0)
        for (BadgeTier tier : tiers) {
            if (total.compareTo(tier.getMinInvested()) >= 0) {
                newTier = tier;
            }
        }

        InvestorBadge badge = investorBadgeRepository.findById(investorId).orElse(null);
        Integer oldTierId = badge != null ? badge.getTierId() : null;

        if (badge == null) {
            badge = new InvestorBadge();
            badge.setInvestorId(investorId);
        }
        badge.setTotalInvested(total);
        badge.setTierId(newTier.getTierId());
        investorBadgeRepository.save(badge);

        boolean leveledUp = oldTierId == null ? newTier.getTierId() > 1 : !newTier.getTierId().equals(oldTierId);
        if (leveledUp) {
            notify(investorId, newTier.getIcon(), "Badge Upgraded!",
                    "Congratulations! You are now a " + newTier.getTierName() + " Investor.");
        }
    }

    // ------------------------------------------------------------------
    // Helper: reuse the existing notifications table/feature for every
    // equity-related event (new investment, dilution, badge upgrade).
    // ------------------------------------------------------------------
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