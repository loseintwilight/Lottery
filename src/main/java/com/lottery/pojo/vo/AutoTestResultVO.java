package com.lottery.pojo.vo;

import java.util.List;

public class AutoTestResultVO {
    private String summary;
    private String drawNo;
    private List<Integer> winningNumbers;
    private int totalUsers;
    private int totalTickets;
    private List<WinnerVO> grandPrizeWinners;
    private List<WinnerVO> firstPrizeWinners;
    private long duration;

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDrawNo() { return drawNo; }
    public void setDrawNo(String drawNo) { this.drawNo = drawNo; }
    public List<Integer> getWinningNumbers() { return winningNumbers; }
    public void setWinningNumbers(List<Integer> winningNumbers) { this.winningNumbers = winningNumbers; }
    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
    public int getTotalTickets() { return totalTickets; }
    public void setTotalTickets(int totalTickets) { this.totalTickets = totalTickets; }
    public List<WinnerVO> getGrandPrizeWinners() { return grandPrizeWinners; }
    public void setGrandPrizeWinners(List<WinnerVO> grandPrizeWinners) { this.grandPrizeWinners = grandPrizeWinners; }
    public List<WinnerVO> getFirstPrizeWinners() { return firstPrizeWinners; }
    public void setFirstPrizeWinners(List<WinnerVO> firstPrizeWinners) { this.firstPrizeWinners = firstPrizeWinners; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
}