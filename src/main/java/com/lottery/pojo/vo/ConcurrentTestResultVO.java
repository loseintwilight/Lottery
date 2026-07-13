package com.lottery.pojo.vo;

import java.util.List;

public class ConcurrentTestResultVO {
    private int concurrentCount;
    private int ticketsPerClient;
    private int totalRequests;
    private int successCount;
    private int failCount;
    private long totalTimeMs;
    private String summary;
    private List<ClientResult> details;

    public int getConcurrentCount() { return concurrentCount; }
    public void setConcurrentCount(int concurrentCount) { this.concurrentCount = concurrentCount; }
    public int getTicketsPerClient() { return ticketsPerClient; }
    public void setTicketsPerClient(int ticketsPerClient) { this.ticketsPerClient = ticketsPerClient; }
    public int getTotalRequests() { return totalRequests; }
    public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }
    public long getTotalTimeMs() { return totalTimeMs; }
    public void setTotalTimeMs(long totalTimeMs) { this.totalTimeMs = totalTimeMs; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<ClientResult> getDetails() { return details; }
    public void setDetails(List<ClientResult> details) { this.details = details; }

    public static class ClientResult {
        private int clientIndex;
        private String username;
        private boolean success;
        private String message;
        private String numbers;

        public int getClientIndex() { return clientIndex; }
        public void setClientIndex(int clientIndex) { this.clientIndex = clientIndex; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getNumbers() { return numbers; }
        public void setNumbers(String numbers) { this.numbers = numbers; }
    }
}