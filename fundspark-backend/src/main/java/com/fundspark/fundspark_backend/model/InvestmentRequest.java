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
@Table(name = "investment_requests")
public class InvestmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @Column(name = "investor_id", nullable = false)
    private Integer investorId;

    @Column(name = "startup_id", nullable = false)
    private Integer startupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "investor_type", nullable = false, length = 20)
    private InvestorType investorType;

    @Column(name = "intended_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal intendedAmount;

    @Column(nullable = false, length = 500)
    private String purpose;

    @Column(length = 500)
    private String experience;

    @Column(name = "message_to_founder", length = 500)
    private String messageToFounder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.pending;

    @Column(name = "rejection_reason", length = 40)
    private String rejectionReason;

    @Column(name = "rejection_message", length = 500)
    private String rejectionMessage;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "can_reapply_at")
    private LocalDateTime canReapplyAt;

    public enum InvestorType { individual, angel, firm }
    public enum Status { pending, accepted, rejected, used, expired }

   

    // ---------- Getters & Setters ----------

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public Integer getInvestorId() { return investorId; }
    public void setInvestorId(Integer investorId) { this.investorId = investorId; }

    public Integer getStartupId() { return startupId; }
    public void setStartupId(Integer startupId) { this.startupId = startupId; }

    public InvestorType getInvestorType() { return investorType; }
    public void setInvestorType(InvestorType investorType) { this.investorType = investorType; }

    public BigDecimal getIntendedAmount() { return intendedAmount; }
    public void setIntendedAmount(BigDecimal intendedAmount) { this.intendedAmount = intendedAmount; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getMessageToFounder() { return messageToFounder; }
    public void setMessageToFounder(String messageToFounder) { this.messageToFounder = messageToFounder; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getRejectionMessage() { return rejectionMessage; }
    public void setRejectionMessage(String rejectionMessage) { this.rejectionMessage = rejectionMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    public LocalDateTime getCanReapplyAt() { return canReapplyAt; }
    public void setCanReapplyAt(LocalDateTime canReapplyAt) { this.canReapplyAt = canReapplyAt; }
}