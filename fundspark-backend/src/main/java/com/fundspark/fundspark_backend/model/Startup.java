package com.fundspark.fundspark_backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "startups")
public class Startup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "startup_id")
    private Integer startupId;

    @Column(name = "founder_id", nullable = false)
    private Integer founderId;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "pitch_video_url")
    private String pitchVideoUrl;

    @Column(name = "business_plan_url")
    private String businessPlanUrl;

    @Column(name = "funding_goal", nullable = false, precision = 14, scale = 2)
    private BigDecimal fundingGoal;

    @Column(name = "amount_raised", precision = 14, scale = 2)
    private BigDecimal amountRaised = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(length = 120)
    private String location;

    @Column(name = "startup_score")
    private Byte startupScore = 0;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "is_trending")
    private Boolean isTrending = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status = Status.pending;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public enum Status { draft, pending, live, closed, rejected }

    // ---------- Getters & Setters ----------

    public Integer getStartupId() { return startupId; }
    public void setStartupId(Integer startupId) { this.startupId = startupId; }

    public Integer getFounderId() { return founderId; }
    public void setFounderId(Integer founderId) { this.founderId = founderId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public String getPitchVideoUrl() { return pitchVideoUrl; }
    public void setPitchVideoUrl(String pitchVideoUrl) { this.pitchVideoUrl = pitchVideoUrl; }

    public String getBusinessPlanUrl() { return businessPlanUrl; }
    public void setBusinessPlanUrl(String businessPlanUrl) { this.businessPlanUrl = businessPlanUrl; }

    public BigDecimal getFundingGoal() { return fundingGoal; }
    public void setFundingGoal(BigDecimal fundingGoal) { this.fundingGoal = fundingGoal; }

    public BigDecimal getAmountRaised() { return amountRaised; }
    public void setAmountRaised(BigDecimal amountRaised) { this.amountRaised = amountRaised; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Byte getStartupScore() { return startupScore; }
    public void setStartupScore(Byte startupScore) { this.startupScore = startupScore; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Boolean getIsTrending() { return isTrending; }
    public void setIsTrending(Boolean isTrending) { this.isTrending = isTrending; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
