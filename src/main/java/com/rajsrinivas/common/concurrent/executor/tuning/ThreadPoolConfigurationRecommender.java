package com.rajsrinivas.common.concurrent.executor.tuning;

import com.rajsrinivas.common.concurrent.executor.parameters.ExecutorParameters;
import com.rajsrinivas.common.concurrent.executor.parameters.TunableParameters;

/**
 * Simple interface for clarity. stitched together with {@link TaskExecutionEventListener}
 * provides one stop for listening and recommending parameters
 */
public interface ThreadPoolConfigurationRecommender {
    /**
     * Implementations should return {@link TunableParameters}
     * that evaluates passed in current parameters.
     */
    TunableParameters getRecommendation(ExecutorParameters executorParameters);
}
