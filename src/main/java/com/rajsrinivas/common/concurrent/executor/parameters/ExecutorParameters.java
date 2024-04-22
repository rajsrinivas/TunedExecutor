package com.rajsrinivas.common.concurrent.executor.parameters;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorParameters {
    public static final TimeUnit TIME_UNIT_FOR_TIMEOUT = TimeUnit.MILLISECONDS;

    private final int currentPoolSize;
    private final int queueSize;
    private final int activeThreadCount;
    private TunableParameters tunableParameters;

    public ExecutorParameters(ThreadPoolExecutor threadPoolExecutor){
        this(threadPoolExecutor.getPoolSize(),threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getMaximumPoolSize(), threadPoolExecutor.getQueue().size(),
                threadPoolExecutor.getKeepAliveTime(ExecutorParameters.TIME_UNIT_FOR_TIMEOUT),
                threadPoolExecutor.getActiveCount());
    }

    public ExecutorParameters(int currentPoolSize,
                              int corePoolSize,
                              int maxPoolSize,
                              int queueSize,
                              long keepAliveTime,
                              int activeThreadCount){
        tunableParameters = new TunableParameters(corePoolSize, maxPoolSize, keepAliveTime);
        this.currentPoolSize = currentPoolSize;
        this.queueSize = queueSize;
        this.activeThreadCount = activeThreadCount;
    }

    public int getCurrentPoolSize() {
        return currentPoolSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getActiveThreadCount() {
        return activeThreadCount;
    }
    public int getCorePoolSize(){return tunableParameters.getCorePoolSize();}
    public int getMaxPoolSize() {return tunableParameters.getMaxPoolSize();}
    public long getKeepAliveTime() {return tunableParameters.getKeepAliveTime();}
}
