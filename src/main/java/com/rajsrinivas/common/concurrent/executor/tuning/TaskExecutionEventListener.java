package com.rajsrinivas.common.concurrent.executor.tuning;

public interface TaskExecutionEventListener {
    void requestArrived();
    void requestWaited(long waitDuration);
    void requestCompleted(long processDuration);
    void printSummary();
}
