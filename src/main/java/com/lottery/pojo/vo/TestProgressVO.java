package com.lottery.pojo.vo;

public class TestProgressVO {
    private String phase;       // "idle", "registering", "buying", "drawing", "done"
    private int current;
    private int total;
    private boolean finished;
    private boolean error;
    private String errorMessage;

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public int getCurrent() { return current; }
    public void setCurrent(int current) { this.current = current; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }
    public boolean isError() { return error; }
    public void setError(boolean error) { this.error = error; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}