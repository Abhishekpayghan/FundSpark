package com.fundspark.fundspark_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "equity_history")
public class EquityHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @Column(name = "investor_id", nullable = false)
    private Integer investorId;

    @Column(name = "startup_id", nullable = false)
    private Integer startupId;

    @Column(name = "old_equity_percentage", nullable = false, precision = 6, scale = 3)
    private BigDecimal oldEquityPercentage;

    @Column(name = "new_equity_percentage", nullable = false, precision = 6, scale = 3)
    private BigDecimal newEquityPercentage;

    @Column(name = "change_reason", length = 150)
    private String changeReason;

    @Column(name = "changed_at", insertable = false, updatable = false)
    private LocalDateTime changedAt;

    // ---------- Getters & Setters ----------

    public Integer getHistoryId() { return historyId; }
    public void setHistoryId(Integer historyId) { this.historyId = historyId; }

    public Integer getInvestorId() { return investorId; }
    public void setInvestorId(Integer investorId) { this.investorId = investorId; }

    public Integer getStartupId() { return startupId; }
    public void setStartupId(Integer startupId) { this.startupId = startupId; }

    public BigDecimal getOldEquityPercentage() { return oldEquityPercentage; }
    public void setOldEquityPercentage(BigDecimal oldEquityPercentage) { this.oldEquityPercentage = oldEquityPercentage; }

    public BigDecimal getNewEquityPercentage() { return newEquityPercentage; }
    public void setNewEquityPercentage(BigDecimal newEquityPercentage) { this.newEquityPercentage = newEquityPercentage; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public LocalDateTime getChangedAt() { return changedAt; }
}