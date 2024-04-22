package com.rajsrinivas.common.concurrent.eventrate;

import com.rajsrinivas.common.concurrent.eventrate.stat.CalculatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Measure event rates over various intervals
 * {@Link #event(long)} value passed to all {@link SingleIntervalEventRateTimerTask} on interval
 * Timer. Create one instance for each event, say, arrival rate , process rate, wait rate etc
 */
public class MultiIntervalEventRateEvaluator implements RateProvider {
    private final Logger logger = LoggerFactory.getLogger(MultiIntervalEventRateEvaluator.class);
    private final Timer timer = new Timer();
    Map<Integer, SingleIntervalEventRateTimerTask> statCalculatingTimers = new HashMap();
    private Collection<SingleIntervalEventRateTimerTask> tasks = new ArrayList();

    private final int[] intervalsInSecs;

    /**
     * A list of {@link SingleIntervalEventRateTimerTask} tracking rates of different intervals from
     * single source of event , passed in {@link #event(long)}
     *
     * @param intervalsInSecs
     * @param factory
     */

    public MultiIntervalEventRateEvaluator(int[] intervalsInSecs, CalculatorFactory factory){
        this.intervalsInSecs = Arrays.copyOf(intervalsInSecs, intervalsInSecs.length);
        Arrays.sort(this.intervalsInSecs);
        for(int i =0 ; i < this.intervalsInSecs.length;i++){
            statCalculatingTimers.put(intervalsInSecs[i],
                    new SingleIntervalEventRateTimerTask(timer, intervalsInSecs[i], factory.getInstance()));
        }
        statCalculatingTimers.put(Integer.MAX_VALUE,
                new SingleIntervalEventRateTimerTask(timer,Integer.MAX_VALUE, factory.getInstance()));
        tasks = statCalculatingTimers.values();

    }

    /**
     * An event value, could be process time or wait time or simple coount, they are pased to each
     * {@link SingleIntervalEventRateTimerTask}
     * @param value
     */

    public void event(long value) { tasks.forEach(each -> each.accumulate(value));}
    public TrafficRate getRates(){
        return new TrafficRate(statCalculatingTimers.get(intervalsInSecs[0]).getMostRecentStat(),
                statCalculatingTimers.get(intervalsInSecs[1]).getMostRecentStat());
    }

    /**
     * This method should be called to cancel underlying {@link Timer}
     */

    public void shutdown() {timer.cancel();}

    /**
     * Print traffic stats, main use case debug
     */

    public void summarize(){
        SingleIntervalEventRateTimerTask task = statCalculatingTimers.get(Integer.MAX_VALUE);
        task.run();
        Rate rate = task.getMostRecentStat();
        long total = Double.valueOf(rate.getValue()).longValue();
        if(rate.getCount() != Integer.MAX_VALUE)
            logger.info(" Count: " + rate.getCount() + " Total: " + total);
        else
            logger.info("Total: " + total);
    }
}
