package com.rajsrinivas.common.concurrent.statistics;

import com.rajsrinivas.common.concurrent.eventrate.MultiIntervalEventRateEvaluator;
import com.rajsrinivas.common.concurrent.eventrate.RateProvider;
import com.rajsrinivas.common.concurrent.eventrate.stat.CountPerSecondCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceStatistics {
    private final MultiIntervalEventRateEvaluator arrivalRateEvaluator;
    private final MultiIntervalEventRateEvaluator completionRateEvaluator;
    private final Logger logger = LoggerFactory.getLogger(ServiceStatistics.class);

    public ServiceStatistics(int[] intervals){
        arrivalRateEvaluator = new MultiIntervalEventRateEvaluator(intervals, CountPerSecondCalculator.factory);
        completionRateEvaluator = new MultiIntervalEventRateEvaluator(intervals, CountPerSecondCalculator.factory);
    }

    void requestArrived() { arrivalRateEvaluator.event(1);}
    void requestCompleted() {completionRateEvaluator.event(1);}
    public RateProvider getArrivalRateProvider() { return arrivalRateEvaluator;}
    public RateProvider getCompletionProvider() { return completionRateEvaluator;}

    public void summarize(){
        logger.info("Service statistics");
        logger.info("Arrival Rate:");
        arrivalRateEvaluator.summarize();
        logger.info("Completion rate: ");
        completionRateEvaluator.summarize();
    }

}
