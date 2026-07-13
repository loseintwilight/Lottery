package com.lottery.pojo.vo;

public class ConcurrentTestRequest {
    private int concurrentCount;
    private int ticketsPerClient;

    public int getConcurrentCount() { return concurrentCount; }
    public void setConcurrentCount(int concurrentCount) { this.concurrentCount = concurrentCount; }
    public int getTicketsPerClient() { return ticketsPerClient; }
    public void setTicketsPerClient(int ticketsPerClient) { this.ticketsPerClient = ticketsPerClient; }
}