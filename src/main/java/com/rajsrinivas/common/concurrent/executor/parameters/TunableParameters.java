package com.rajsrinivas.common.concurrent.executor.parameters;

public class TunableParameters {

    public int corePoolSize;
    public int maxPoolSize;
    public long keepAliveTime;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public TunableParameters(int corePoolSize, int maxPoolSize, long keepAliveTime) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
    }
}
