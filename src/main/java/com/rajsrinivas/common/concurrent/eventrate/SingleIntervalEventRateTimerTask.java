package com.rajsrinivas.common.concurrent.eventrate;

import com.rajsrinivas.common.concurrent.eventrate.stat.ResetableStatCalculator;
import com.rajsrinivas.common.concurrent.eventrate.stat.StatProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class SingleIntervalEventRateTimerTask extends TimerTask implements StatProvider {
    private ResetableStatCalculator statCalculator;
    private Rate mostRecentStat;
    private AtomicLong count = new AtomicLong();
    private int intervalInSeconds;

    public SingleIntervalEventRateTimerTask(final Timer timer, int interval,
                                            ResetableStatCalculator statCalculator){
        if(interval < Integer.MAX_VALUE){
            timer.scheduleAtFixedRate(this, interval * 1000, interval * 1000);
        }
        this.statCalculator = statCalculator;
        this.intervalInSeconds = interval;
    }

    /**
     * Accumulates only over an interval and rese after interval elapsed
     * Accumulates a value that is numerator in {@link Rate} object.
     *
     * @param value
     */

    public void accumulate(long value) {
        statCalculator.accumulate(value);
        count.incrementAndGet();
    }

    @Override
    public void run() {
        try {
            mostRecentStat = statCalculator.getValueAndReset(intervalInSeconds);
        }catch(Throwable t){
            t.printStackTrace();
        }
    }
    @Override
    public Rate getMostRecentStat() { return mostRecentStat;}
}
