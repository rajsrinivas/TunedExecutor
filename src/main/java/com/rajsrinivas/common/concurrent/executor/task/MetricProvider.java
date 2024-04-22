package com.rajsrinivas.common.concurrent.executor.task;

public interface MetricProvider {
    long getProcessTime();
    long getWaitTime();
}
