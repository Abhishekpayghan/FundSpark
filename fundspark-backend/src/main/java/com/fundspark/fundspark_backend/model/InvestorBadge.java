package com.fundspark.fundspark_backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "investor_badges")
public class InvestorBadge {

    // Not auto-generated: this IS the investor's user_id (one badge row per investor)
    @Id
    @Column(name = "investor_id")
    private Integer investorId;

    @Column(name = "total_invested", precision = 14, scale = 2)
    private BigDecimal totalInvested = BigDecimal.ZERO;

    @Column(name = "tier_id", nullable = false)
    private Integer tierId;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // ---------- Getters & Setters ----------

    public Integer getInvestorId() { return investorId; }
    public void setInvestorId(Integer investorId) { this.investorId = investorId; }

    public BigDecimal getTotalInvested() { return totalInvested; }
    public void setTotalInvested(BigDecimal totalInvested) { this.totalInvested = totalInvested; }

    public Integer getTierId() { return tierId; }
    public void setTierId(Integer tierId) { this.tierId = tierId; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
