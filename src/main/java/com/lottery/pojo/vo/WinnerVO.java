package com.lottery.pojo.vo;

import java.math.BigDecimal;

public class WinnerVO {
    private Integer userId;
    private String username;
    private String prizeLevel;
    private BigDecimal prizeAmount;
    private Integer betCount;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPrizeLevel() { return prizeLevel; }
    public void setPrizeLevel(String prizeLevel) { this.prizeLevel = prizeLevel; }
    public BigDecimal getPrizeAmount() { return prizeAmount; }
    public void setPrizeAmount(BigDecimal prizeAmount) { this.prizeAmount = prizeAmount; }
    public Integer getBetCount() { return betCount; }
    public void setBetCount(Integer betCount) { this.betCount = betCount; }
}