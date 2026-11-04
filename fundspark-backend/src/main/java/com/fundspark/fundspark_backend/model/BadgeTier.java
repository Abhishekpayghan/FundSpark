package com.fundspark.fundspark_backend.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "badge_tiers")
public class BadgeTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tier_id")
    private Integer tierId;

    @Column(name = "tier_name", nullable = false, length = 30)
    private String tierName;

    @Column(name = "min_invested", nullable = false, precision = 14, scale = 2)
    private BigDecimal minInvested;

    @Column(length = 40)
    private String icon;

    @Column(length = 20)
    private String color;

    // ---------- Getters & Setters ----------

    public Integer getTierId() { return tierId; }
    public void setTierId(Integer tierId) { this.tierId = tierId; }

    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }

    public BigDecimal getMinInvested() { return minInvested; }
    public void setMinInvested(BigDecimal minInvested) { this.minInvested = minInvested; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}