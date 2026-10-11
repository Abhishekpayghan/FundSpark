package com.fundspark.fundspark_backend.controller;

import java.math.BigDecimal;
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

import com.fundspark.fundspark_backend.model.Investment;
import com.fundspark.fundspark_backend.model.Notification;
import com.fundspark.fundspark_backend.model.Startup;
import com.fundspark.fundspark_backend.model.ValuationHistory;
import com.fundspark.fundspark_backend.repository.InvestmentRepository;
import com.fundspark.fundspark_backend.repository.NotificationRepository;
import com.fundspark.fundspark_backend.repository.StartupRepository;
import com.fundspark.fundspark_backend.repository.ValuationHistoryRepository;

// Powers the "Valuation History Timeline" feature.
@RestController
@RequestMapping("/api/valuation")
@CrossOrigin(origins = "*")
public class ValuationHistoryController {

    @Autowired
    private ValuationHistoryRepository valuationHistoryRepository;

    @Autowired
    private StartupRepository startupRepository;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // GET /api/valuation/{startupId}
    // Full valuation timeline for one startup, oldest first — feeds the
    // "Seed -> Series A -> Series B" step chart on Startup Details / Analytics.
    @GetMapping("/{startupId}")
    public List<Map<String, Object>> getTimeline(@PathVariable Integer startupId) {
        String sql =
            "SELECT round_name AS roundName, valuation_amount AS valuationAmount, changed_at AS changedAt " +
            "FROM valuation_history WHERE startup_id = ? ORDER BY changed_at ASC";
        return jdbcTemplate.queryForList(sql, startupId);
    }

    // POST /api/valuation/{startupId}/round   -> Founder "Add Valuation Round" form
    // Body example: { "roundName": "Series A", "valuationAmount": 12000000 }
    //
    // This DOES NOT retroactively change anyone's equity % — a %, once granted at
    // an earlier round's valuation, stays what it was granted as. It only changes
    // the valuation used for any FUTURE investment from this point on, and logs the
    // round in the timeline.
    @Transactional
    @PostMapping("/{startupId}/round")
    public ResponseEntity<?> addRound(@PathVariable Integer startupId, @RequestBody Map<String, Object> body) {
        Startup startup = startupRepository.findById(startupId).orElse(null);
        if (startup == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Startup not found");
        }

        String roundName = String.valueOf(body.get("roundName"));
        BigDecimal newValuation = new BigDecimal(String.valueOf(body.get("valuationAmount")));

        startup.setValuation(newValuation);

        // NEW: optionally add this round's raise amount to the startup's total funding goal,
        // so the public funding-progress bar reflects the larger total the founder is now
        // raising (rather than silently leaving the old, smaller goal displayed).
        Object raiseAmountObj = body.get("raiseAmount");
        Object addToGoalObj = body.get("addToFundingGoal");
        boolean addToGoal = addToGoalObj != null && Boolean.parseBoolean(String.valueOf(addToGoalObj));
        if (addToGoal && raiseAmountObj != null) {
            BigDecimal raiseAmount = new BigDecimal(String.valueOf(raiseAmountObj));
            startup.setFundingGoal(startup.getFundingGoal().add(raiseAmount));
        }

        startupRepository.save(startup);

        ValuationHistory vh = new ValuationHistory();
        vh.setStartupId(startupId);
        vh.setRoundName(roundName);
        vh.setValuationAmount(newValuation);
        ValuationHistory savedRound = valuationHistoryRepository.save(vh);

        // Let every existing investor in this startup know the valuation moved
        List<Investment> investments = investmentRepository.findByStartupId(startupId);
        java.util.Set<Integer> notifiedInvestors = new java.util.HashSet<>();
        for (Investment inv : investments) {
            if (inv.getStatus() != Investment.Status.confirmed) continue;
            if (!notifiedInvestors.add(inv.getInvestorId())) continue; // one notification per investor

            Notification n = new Notification();
            n.setUserId(inv.getInvestorId());
            n.setIcon("trending-up");
            n.setTitle("New Valuation Round");
            n.setMessage(startup.getName() + " raised a new round: " + roundName +
                    " at a valuation of " + newValuation);
            n.setIsRead(false);
            notificationRepository.save(n);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedRound);
    }
}