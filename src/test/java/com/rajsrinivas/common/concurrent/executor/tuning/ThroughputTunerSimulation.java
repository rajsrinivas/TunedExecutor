package com.rajsrinivas.common.concurrent.executor.tuning;

import com.rajsrinivas.common.concurrent.eventrate.TrafficRate;
import com.rajsrinivas.common.concurrent.executor.NamedThreadFactory;
import com.rajsrinivas.common.concurrent.executor.TimedThreadPoolExecutor;
import com.rajsrinivas.common.concurrent.executor.parameters.ExecutorParameters;
import com.rajsrinivas.common.concurrent.traffic.RecordableRandomNumberIterator;
import com.rajsrinivas.common.concurrent.traffic.TaskFactory;
import com.rajsrinivas.common.concurrent.traffic.TrafficSimulator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;

public class ThroughputTunerSimulation {
    private Logger logger = LoggerFactory.getLogger(ThroughputTunerSimulation.class);

    private RecordableRandomNumberIterator randomNumberIterator =
            new RecordableRandomNumberIterator();
    int[] requestsPerSecond = new int[] {10,50,250,80,500,0,50,100,350};
    int parallelRuns = 3;
    int simulationDurationInSeconds = 2;
    int corePoolSize = 50;
    int maxPoolSize = 100;
    int keepAliveTime = 10;
    int checkFrequencyInSeconds = 5;
    int awaitTerminationTimeInSeconds = 90;// Depends on total simulation and time taken by tasks

    @Test
    public void compareTunerPerformance() throws InterruptedException {
        p("Running simulatation on untuned and auto tuned thread pools\n");
        p("Stats gathered short term every "+ TrafficRate.SHORT_TERM_CHECK_IN_SECONDS
        + " seconds, Long term every " + TrafficRate.LONG_TERM_CHECK_IN_SECONDS
        + " seconds");
        printPoolSettings();
        printSimulationParameters();
        p("\nRunning untuned thread pool");
        executeWithoutTuning();
        p("Replaying same traffic pattern on auto tuned thread pool");
        executeWithTuning();
    }

    private void executeWithTuning() throws InterruptedException {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        boolean autoTune = true;
        ThreadPoolExecutor threadPoolExecutor = new TimedThreadPoolExecutor(corePoolSize,
                maxPoolSize, keepAliveTime, ExecutorParameters.TIME_UNIT_FOR_TIMEOUT,
                queue, new NamedThreadFactory(), checkFrequencyInSeconds, autoTune);
        TrafficSimulator trafficSimulator = new TrafficSimulator(threadPoolExecutor,
                requestsPerSecond, parallelRuns, simulationDurationInSeconds);
        TaskFactory taskFactory = new TaskFactory(randomNumberIterator.getRecording());
        trafficSimulator.simulate(taskFactory);
        threadPoolExecutor.awaitTermination((int) awaitTerminationTimeInSeconds, TimeUnit.SECONDS);
        ((TimedThreadPoolExecutor) threadPoolExecutor).printSummary();
        threadPoolExecutor.shutdown();
    }

    private void printPoolSettings() {
        p("Initial pool settings core : " + corePoolSize + " Max: " + maxPoolSize
        + " Keep alive time: " + keepAliveTime);
    }

    private void printSimulationParameters() {
        StringBuilder builder = new StringBuilder();
        for(int each =0; each < requestsPerSecond.length ; each++){
            builder.append(requestsPerSecond[each] + ",");
        }
        p("Simulating Request: Batches of (" + builder.substring(0, builder.length() -1)
            + ") by "
            + parallelRuns + " parallel threads generate requests for " + simulationDurationInSeconds
            + " seconds.");
    }

    private void executeWithoutTuning() throws InterruptedException {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue();
        boolean autoTune = false;
        ThreadPoolExecutor threadPoolExecutor = new TimedThreadPoolExecutor(
                corePoolSize, maxPoolSize, keepAliveTime, ExecutorParameters.TIME_UNIT_FOR_TIMEOUT,
                queue, new NamedThreadFactory(), checkFrequencyInSeconds,autoTune);
        TrafficSimulator trafficSimulator = new TrafficSimulator(threadPoolExecutor,
                requestsPerSecond, parallelRuns, simulationDurationInSeconds);
        TaskFactory taskFactory = new TaskFactory(randomNumberIterator);
        trafficSimulator.simulate(taskFactory);
        threadPoolExecutor.awaitTermination(awaitTerminationTimeInSeconds, TimeUnit.SECONDS);
        ((TimedThreadPoolExecutor) threadPoolExecutor).printSummary();
        threadPoolExecutor.shutdown();
    }


    <T> void p(T val) {logger.info(val.toString());}
}