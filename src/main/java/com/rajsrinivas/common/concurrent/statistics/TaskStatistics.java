package com.rajsrinivas.common.concurrent.statistics;

import com.rajsrinivas.common.concurrent.eventrate.MultiIntervalEventRateEvaluator;
import com.rajsrinivas.common.concurrent.eventrate.RateProvider;
import com.rajsrinivas.common.concurrent.eventrate.stat.AverageCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskStatistics {
    private final Logger logger = LoggerFactory.getLogger(TaskStatistics.class);
    private final MultiIntervalEventRateEvaluator waitTimeRate;
    private final MultiIntervalEventRateEvaluator processTimeRate;

    public TaskStatistics(int[] intervals){
        waitTimeRate = new MultiIntervalEventRateEvaluator(intervals, AverageCalculator.factory);
        processTimeRate = new MultiIntervalEventRateEvaluator(intervals, AverageCalculator.factory);
    }

    void requestWaited(long waitDuration) { waitTimeRate.event(waitDuration);}
    void requestCompleted(long processDuration) { processTimeRate.event(processDuration);}
    public RateProvider getWaitRateProvider() { return waitTimeRate;}
    public RateProvider getProcessTimeRate() {return processTimeRate;}

    public void summarize(){
        logger.info("Task Statistics:");
        logger.info("Net wait time :");
        waitTimeRate.summarize();
        logger.info("Net process time: ");
        processTimeRate.summarize();
    }

}
