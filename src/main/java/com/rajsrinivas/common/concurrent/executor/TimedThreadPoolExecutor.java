package com.rajsrinivas.common.concurrent.executor;

import com.rajsrinivas.common.concurrent.eventrate.TrafficRate;
import com.rajsrinivas.common.concurrent.executor.task.MetricsGatheringFutureTask;
import com.rajsrinivas.common.concurrent.executor.tuning.ThroughputTuner;
import com.rajsrinivas.common.concurrent.statistics.TrafficStatistics;

import java.util.Timer;
import java.util.concurrent.*;

/**
 * <p>
 *     Implementation of custom thread pool executor that auto configures considering current
 *     traffic conditions.
 *     Internally uses {@link TrafficStatistics} which is both request level listener and implements tuning logic.
 * </p>
 */
public class TimedThreadPoolExecutor extends ThreadPoolExecutor {
    private static final int START_AFTER = (TrafficRate.LONG_TERM_CHECK_IN_SECONDS * 1000) + 100;
    private final Timer timer = new Timer();
    private final TrafficStatistics trafficStatistics = new TrafficStatistics();


    public TimedThreadPoolExecutor(int corePoolSize, int maxPoolSize,
                                   long keepAliveTime,
                                   TimeUnit timeUnit, BlockingQueue<Runnable> queue,
                                   ThreadFactory threadFactory, int tuneFrequencyInSeconds,
                                    boolean applyTunedParameters) {
        super(corePoolSize, maxPoolSize, keepAliveTime, timeUnit, queue, threadFactory);
        ThroughputTuner throughputTuner = new ThroughputTuner(this, applyTunedParameters, trafficStatistics);
        timer.scheduleAtFixedRate(throughputTuner, START_AFTER, tuneFrequencyInSeconds * 1000L);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable){
        trafficStatistics.requestArrived();
        return new MetricsGatheringFutureTask<>(callable);
    }

    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T arg1){
        trafficStatistics.requestArrived();
        return new MetricsGatheringFutureTask<>(runnable, arg1);
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable t) {
        super.afterExecute(runnable,t);
        if (runnable instanceof MetricsGatheringFutureTask<?>){
            MetricsGatheringFutureTask<?> any = (MetricsGatheringFutureTask<?>) runnable;
            trafficStatistics.requestWaited(any.getWaitTime());
            trafficStatistics.requestCompleted(any.getProcessTime());
        }
    }

    public void printSummary() { trafficStatistics.printSummary();}

    public void shutdown(){
        timer.cancel();
        super.shutdown();
    }
}
