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
@Table(name = "valuation_history")
public class ValuationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "valuation_id")
    private Integer valuationId;

    @Column(name = "startup_id", nullable = false)
    private Integer startupId;

    @Column(name = "round_name", nullable = false, length = 60)
    private String roundName;

    @Column(name = "valuation_amount", nullable = false, precision = 16, scale = 2)
    private BigDecimal valuationAmount;

    @Column(name = "changed_at", insertable = false, updatable = false)
    private LocalDateTime changedAt;

    // ---------- Getters & Setters ----------

    public Integer getValuationId() { return valuationId; }
    public void setValuationId(Integer valuationId) { this.valuationId = valuationId; }

    public Integer getStartupId() { return startupId; }
    public void setStartupId(Integer startupId) { this.startupId = startupId; }

    public String getRoundName() { return roundName; }
    public void setRoundName(String roundName) { this.roundName = roundName; }

    public BigDecimal getValuationAmount() { return valuationAmount; }
    public void setValuationAmount(BigDecimal valuationAmount) { this.valuationAmount = valuationAmount; }

    public LocalDateTime getChangedAt() { return changedAt; }
}