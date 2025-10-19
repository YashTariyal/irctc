package com.irctc_backend.irctc.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for reward redemption request
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class RewardRedemptionRequest {
    
    @NotNull(message = "Reward ID is required")
    private Long rewardId;
    
    private String notes;
    
    // Constructors
    public RewardRedemptionRequest() {}
    
    public RewardRedemptionRequest(Long rewardId) {
        this.rewardId = rewardId;
    }
    
    public RewardRedemptionRequest(Long rewardId, String notes) {
        this.rewardId = rewardId;
        this.notes = notes;
    }
    
    // Getters and Setters
    public Long getRewardId() {
        return rewardId;
    }
    
    public void setRewardId(Long rewardId) {
        this.rewardId = rewardId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
