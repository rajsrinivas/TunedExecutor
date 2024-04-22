package com.rajsrinivas.common.concurrent.statistics;

import com.rajsrinivas.common.concurrent.eventrate.RateProvider;
import com.rajsrinivas.common.concurrent.eventrate.TrafficRate;
import com.rajsrinivas.common.concurrent.executor.parameters.ExecutorParameters;
import com.rajsrinivas.common.concurrent.executor.parameters.TunableParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ThreadPoolStateAnalyzer {
    private static final String FIELD_FORMAT = "%-20s";
    private static final String FIELD_FORMAT_LONG = "%-25s";
    private static final String FIELD_FORMAT_INT_COL = "%-10s";
    private static final double MAX_POOL_RATE_INCREMENT = 1.25;
    private static final double MAX_CONCURRENT_THREADS_ALLOWED = 1000;
    private static final double QUEUE_INCREMENT_RATE = 0.25;

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolStateAnalyzer.class);
    private static final double POOL_INCREMENT_BASED_ON_ARRIVAL_RATE = 0.25;
    private static final double MAX_POOL_RATE_DECREMENT = 0.25;

    private final RateProvider arrivalRate;
    private final RateProvider processRate;
    private final RateProvider waitTimeRate;
    private final RateProvider processTimeRate;

    private int count = 1;
    private static final int INT_SIZE = 10;

    public ThreadPoolStateAnalyzer(ServiceStatistics serviceStatistics, TaskStatistics taskStatistics) {
        this.arrivalRate = serviceStatistics.getArrivalRateProvider();
        this.processRate = serviceStatistics.getCompletionProvider();
        this.waitTimeRate = taskStatistics.getWaitRateProvider();
        this.processTimeRate = taskStatistics.getProcessTimeRate();

        p("Legend: () --> Long term rates");
        p("#   " + String.format(FIELD_FORMAT, "Arrival Rate") +
                String.format(FIELD_FORMAT, "Process Rate") +
                String.format(FIELD_FORMAT_LONG, "Process Time") +
                String.format(FIELD_FORMAT_LONG, "Wait Time") +
                String.format(FIELD_FORMAT_INT_COL, "Core") +
                String.format(FIELD_FORMAT_INT_COL, "Current") +
                String.format(FIELD_FORMAT_INT_COL, "Queue") +
                String.format(FIELD_FORMAT_INT_COL, "Active") +
                String.format(FIELD_FORMAT_INT_COL, "State"));
    }

    private boolean hasPastThreshold(TrafficRate rates, double rate) {
        double rateShortTerm = rates.getShortTerm().getRate();
        double rateLongTerm = rates.getLongTerm().getRate();
        boolean rateIncreased = rateShortTerm > (rateLongTerm * rate);
        return rateIncreased;
    }

    TunableParameters analyzeAndRecommendNewParameters(double arrivalThreshold,
                                                       double waitThreshold,
                                                       ExecutorParameters currentExecutorParameters) {
        int queuesize = currentExecutorParameters.getQueueSize();
        int currentPoolSize = currentExecutorParameters.getCurrentPoolSize();
        int activeThreadCount = currentExecutorParameters.getActiveThreadCount();

        TrafficRate arrivalRates = arrivalRate.getRates();
        TrafficRate processRates = processRate.getRates();
        TrafficRate waitTimeRates = waitTimeRate.getRates();
        TrafficRate processTimeRates = processTimeRate.getRates();

        boolean arrivalRateIncreased = hasPastThreshold(arrivalRates, arrivalThreshold);
        boolean waitTimeIncreased = hasPastThreshold(waitTimeRates, waitThreshold);
        ThreadPoolState threadPoolState =
                ThreadPoolState.to_System_State(arrivalRateIncreased, waitTimeIncreased);
        double avgArrivalRateShortTerm = arrivalRates.getShortTerm().getRate();
        double avgWaitRateShortTerm = waitTimeRates.getShortTerm().getRate();

        p(formatInt(count++, 5) + formatCountRate(arrivalRates) + formatCountRate(processRates)
                + formatRates(processTimeRates) + formatRates(waitTimeRates)
                + formatInt(currentExecutorParameters.getCorePoolSize(), INT_SIZE)
                + formatInt(currentPoolSize, INT_SIZE) + formatInt(queuesize, INT_SIZE)
                + formatInt(activeThreadCount, INT_SIZE)
                + String.format(FIELD_FORMAT, threadPoolState));
        TunableParameters tunableParameters = null;
                long currentThreadIdleTimeout = currentExecutorParameters.getKeepAliveTime();
        switch (threadPoolState) {
            case ARRIVAL_RATE_AND_WAIT_TIME_INCREASED:
                tunableParameters = whenArrivalTimeAndWaitTimeIncreased(queuesize,
                        avgArrivalRateShortTerm, avgWaitRateShortTerm);
                break;
            case ARRIVAL_RATE_INCREASED:
                tunableParameters = whenArrivalTimeAloneIncreased(avgArrivalRateShortTerm, currentThreadIdleTimeout);
                break;
            case WAIT_TIME_INCREASED:
                tunableParameters = whenWaitTimeAloneIncreased(queuesize, currentPoolSize, currentThreadIdleTimeout);
                break;
            case SYSTEM_STEADY:
                if (avgWaitRateShortTerm > 0 || queuesize > 0) {
                    tunableParameters = whenSteadyStateWithWaitTimeAndNonEmptyQueue(queuesize,
                            currentPoolSize, currentThreadIdleTimeout);
                } else {
                    long currentCorePoolSize = currentExecutorParameters.getCorePoolSize();
                    if (avgArrivalRateShortTerm < 0.5 * currentCorePoolSize) {
                        tunableParameters = whenSteadyStateArrivalLessThanCurrentLiveThreads(currentPoolSize,
                                currentThreadIdleTimeout);
                    } else {
                        tunableParameters = getInitialTunableParameters(currentExecutorParameters.getCorePoolSize(),
                                currentExecutorParameters.getMaxPoolSize(), currentExecutorParameters.getKeepAliveTime());
                    }
                }
                break;
            default:
                tunableParameters = getInitialTunableParameters(currentExecutorParameters.getCorePoolSize(),
                        currentExecutorParameters.getMaxPoolSize(), currentExecutorParameters.getKeepAliveTime());
        }
        return tunableParameters;
    }

    private TunableParameters getInitialTunableParameters(int corePoolSize, int maxPoolSize, long keepAliveTime) {
        return new TunableParameters(corePoolSize,
                maxPoolSize,
                keepAliveTime);
    }

    private TunableParameters whenSteadyStateArrivalLessThanCurrentLiveThreads(int currentPoolSize,
                                                                               long newTimeout) {
        int newCorePoolSize = currentPoolSize;
        int newMaxPoolSize = currentPoolSize;
        if (currentPoolSize >= 10) {
            newCorePoolSize = (int) (currentPoolSize * MAX_POOL_RATE_DECREMENT);
            newMaxPoolSize = incrementMaxPool(newCorePoolSize);
        }
        return getInitialTunableParameters(newCorePoolSize, newMaxPoolSize, (int) (newTimeout * 0.8));
    }

    private TunableParameters whenArrivalTimeAndWaitTimeIncreased(int queuesize, double avgArrivalRateShortTerm,
                                                                  double avgWaitRateShortTerm) {
        int newCorePoolSize = (int) (avgArrivalRateShortTerm * POOL_INCREMENT_BASED_ON_ARRIVAL_RATE + (queuesize * QUEUE_INCREMENT_RATE));
        int newMaxPoolSize = incrementMaxPool(newCorePoolSize);
        long newTimeout = (long) avgWaitRateShortTerm;
        return getInitialTunableParameters(newCorePoolSize, newMaxPoolSize, newTimeout);
    }

    private TunableParameters whenArrivalTimeAloneIncreased(double avgArrivalRateShortTerm,
                                                            long newTimeout) {
        int newCorePoolSize = (int) (avgArrivalRateShortTerm);
        int newMaxPoolSize = incrementMaxPool(newCorePoolSize);
        return getInitialTunableParameters(newCorePoolSize, newMaxPoolSize, (int) (newTimeout * 0.8));

    }

    private TunableParameters whenWaitTimeAloneIncreased(int queuesize, int currentPoolSize,
                                                         long newTimeout) {
        int newCorePoolSize = currentPoolSize + (int) (queuesize * QUEUE_INCREMENT_RATE);
        int newMaxPoolSize = incrementMaxPool(newCorePoolSize);
        return getInitialTunableParameters(newCorePoolSize, newMaxPoolSize, (int) (newTimeout * 0.8));
    }

    private TunableParameters whenSteadyStateWithWaitTimeAndNonEmptyQueue(int queuesize, int currentPoolSize,
                                                                          long newTimeout) {
        int newCorePoolSize = currentPoolSize + (int) (queuesize * QUEUE_INCREMENT_RATE);
        int newMaxPoolSize = incrementMaxPool(newCorePoolSize);
        return getInitialTunableParameters(newCorePoolSize, newMaxPoolSize, (int) (newTimeout * 0.8));
    }

    private String formatInt(int value, int spacing) {
        String dynamicFormat = "%-" + spacing + "s";
        return String.format(dynamicFormat, value);
    }

    private int incrementMaxPool(int newCorePoolSize) {
        return (int) (newCorePoolSize * MAX_POOL_RATE_INCREMENT);
    }

    private String formatRates(TrafficRate rates) {
        return String.format(FIELD_FORMAT_LONG, format(rates.getShortTerm().getRate()) + "(" +
                format(rates.getLongTerm().getRate()) + ")");

    }

    private String format(double value) {
        NumberFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(value);
    }

    private String formatCountRate(TrafficRate rates) {
        return String.format(FIELD_FORMAT, format(rates.getShortTerm().getRate()) + "(" +
                format(rates.getLongTerm().getRate()) + ")");
    }

    TunableParameters rationalizeRecommendedParameters(TunableParameters executionParameters) {

        int newCorePoolSize = executionParameters.getCorePoolSize();
        int newMaxPoolSize = executionParameters.getMaxPoolSize();
        long newTimeOut = executionParameters.getKeepAliveTime();

        double avgProcessRateShortTerm = processTimeRate.getRates().getShortTerm().getRate();
        double avgProcessRateLongTerm = processRate.getRates().getLongTerm().getRate();
        double avgArrivalRateShortTerm = arrivalRate.getRates().getShortTerm().getRate();

        long expectedInFlight = (long) (avgArrivalRateShortTerm * avgProcessRateShortTerm);
        if (expectedInFlight != 0 && newCorePoolSize > expectedInFlight * 1.5) {
            if (avgProcessRateLongTerm > avgProcessRateShortTerm) {
                newCorePoolSize = (int) (expectedInFlight * 1.25);
                newMaxPoolSize = incrementMaxPool(newCorePoolSize);
                newTimeOut *= 1.5;
            }
        }
        if (newCorePoolSize > (int) (MAX_CONCURRENT_THREADS_ALLOWED) * 0.8) {
            newCorePoolSize = (int) (MAX_CONCURRENT_THREADS_ALLOWED * 0.5);
            newMaxPoolSize = incrementMaxPool(newCorePoolSize);
            newTimeOut *= 1.5;
            // Here we assume that when system is maxed out, thread destroy /create dont hinder more.
        }

        if (newCorePoolSize == 0) newCorePoolSize = 10;
        if (newMaxPoolSize == 0) newMaxPoolSize = 15;
        return getInitialTunableParameters(newCorePoolSize, newMaxPoolSize, newTimeOut);
    }
    <T> void p(T val) { logger.info(val.toString());}
}
