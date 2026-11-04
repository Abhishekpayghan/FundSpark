package com.fundspark.fundspark_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "investments")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_id")
    private Integer investmentId;

    @Column(name = "investor_id", nullable = false)
    private Integer investorId;

    @Column(name = "startup_id", nullable = false)
    private Integer startupId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    // ---- NEW: equity feature field ----
    // Calculated by the backend as (amount / startup.valuation) * 100 at investment time
    @Column(name = "equity_percentage", precision = 6, scale = 3)
    private BigDecimal equityPercentage = BigDecimal.ZERO;
    // ------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod = PaymentMethod.upi;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status = Status.pending;

    @Column(name = "invested_at", updatable = false, insertable = false)
    private LocalDateTime investedAt;

    public enum PaymentMethod { upi, netbanking, card }
    public enum Status { pending, confirmed, failed }

    // ---------- Getters & Setters ----------

    public Integer getInvestmentId() { return investmentId; }
    public void setInvestmentId(Integer investmentId) { this.investmentId = investmentId; }

    public Integer getInvestorId() { return investorId; }
    public void setInvestorId(Integer investorId) { this.investorId = investorId; }

    public Integer getStartupId() { return startupId; }
    public void setStartupId(Integer startupId) { this.startupId = startupId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getEquityPercentage() { return equityPercentage; }
    public void setEquityPercentage(BigDecimal equityPercentage) { this.equityPercentage = equityPercentage; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getInvestedAt() { return investedAt; }
}