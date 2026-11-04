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
@Table(name = "equity_certificates")
public class EquityCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Integer certificateId;

    // Left null on insert — the DB trigger (trg_certificate_code) auto-fills it
    @Column(name = "certificate_code", unique = true, length = 30, insertable = false, updatable = false)
    private String certificateCode;

    @Column(name = "investment_id", nullable = false, unique = true)
    private Integer investmentId;

    @Column(name = "investor_id", nullable = false)
    private Integer investorId;

    @Column(name = "startup_id", nullable = false)
    private Integer startupId;

    @Column(name = "equity_percentage", nullable = false, precision = 6, scale = 3)
    private BigDecimal equityPercentage;

    @Column(name = "amount_invested", nullable = false, precision = 14, scale = 2)
    private BigDecimal amountInvested;

    @Column(name = "issue_date", insertable = false, updatable = false)
    private LocalDateTime issueDate;

    // ---------- Getters & Setters ----------

    public Integer getCertificateId() { return certificateId; }
    public void setCertificateId(Integer certificateId) { this.certificateId = certificateId; }

    public String getCertificateCode() { return certificateCode; }

    public Integer getInvestmentId() { return investmentId; }
    public void setInvestmentId(Integer investmentId) { this.investmentId = investmentId; }

    public Integer getInvestorId() { return investorId; }
    public void setInvestorId(Integer investorId) { this.investorId = investorId; }

    public Integer getStartupId() { return startupId; }
    public void setStartupId(Integer startupId) { this.startupId = startupId; }

    public BigDecimal getEquityPercentage() { return equityPercentage; }
    public void setEquityPercentage(BigDecimal equityPercentage) { this.equityPercentage = equityPercentage; }

    public BigDecimal getAmountInvested() { return amountInvested; }
    public void setAmountInvested(BigDecimal amountInvested) { this.amountInvested = amountInvested; }

    public LocalDateTime getIssueDate() { return issueDate; }
}