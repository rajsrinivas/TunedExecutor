package com.rajsrinivas.common.concurrent.statistics;

import com.rajsrinivas.common.concurrent.eventrate.TrafficRate;
import com.rajsrinivas.common.concurrent.executor.parameters.ExecutorParameters;
import com.rajsrinivas.common.concurrent.executor.parameters.TunableParameters;
import com.rajsrinivas.common.concurrent.executor.tuning.TaskExecutionEventListener;
import com.rajsrinivas.common.concurrent.executor.tuning.ThreadPoolConfigurationRecommender;

import java.util.concurrent.atomic.AtomicLong;

public class TrafficStatistics implements TaskExecutionEventListener, ThreadPoolConfigurationRecommender {

    public static final double WAIT_RATE_THRESHOLD = 1.2;
    public static final double ARRIVAL_RATE_THRESHOLD = 1.25;

    private final AtomicLong processed = new AtomicLong(0);
    private final AtomicLong arrived = new AtomicLong(0);

    private static final int[] INTERVALS = new int[]{
            TrafficRate.SHORT_TERM_CHECK_IN_SECONDS,
            TrafficRate.LONG_TERM_CHECK_IN_SECONDS
    };
    private final TaskStatistics taskStatistics = new TaskStatistics(INTERVALS);
    private final ServiceStatistics serviceStatistics = new ServiceStatistics(INTERVALS);
    private final ThreadPoolStateAnalyzer systemStateAnalyzer = new ThreadPoolStateAnalyzer(serviceStatistics, taskStatistics);
    @Override
    public void requestArrived() {
        serviceStatistics.requestArrived();
        arrived.incrementAndGet();
    }

    @Override
    public void requestWaited(long waitDuration) {
        taskStatistics.requestWaited(waitDuration);
    }

    @Override
    public void requestCompleted(long processDuration) {
        serviceStatistics.requestCompleted();
        taskStatistics.requestCompleted(processDuration);
        processed.incrementAndGet();
    }

    @Override
    public void printSummary() {
        serviceStatistics.summarize();
        taskStatistics.summarize();
    }

    @Override
    public TunableParameters getRecommendation(ExecutorParameters executorParameters) {
        TunableParameters tunableParameters =
                systemStateAnalyzer.analyzeAndRecommendNewParameters(ARRIVAL_RATE_THRESHOLD,
                        WAIT_RATE_THRESHOLD, executorParameters);
        return systemStateAnalyzer.rationalizeRecommendedParameters(tunableParameters);
    }
}
