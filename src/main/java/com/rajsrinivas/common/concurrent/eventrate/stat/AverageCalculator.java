package com.rajsrinivas.common.concurrent.eventrate.stat;

import com.rajsrinivas.common.concurrent.eventrate.Rate;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 *     Calculates an average by keeping track of a total and counter.
 * </p>
 */
public class AverageCalculator implements ResetableStatCalculator {

    public static Factory factory = new Factory();
    private static class Factory implements CalculatorFactory {
        @Override
        public AverageCalculator getInstance() { return new AverageCalculator();}
    }

    private AtomicLong value = new AtomicLong(0);
    private AtomicLong counter = new AtomicLong(0);

    @Override
    public void accumulate(long value) {
        this.counter.incrementAndGet();
        this.value.addAndGet(value);
    }

    public synchronized Rate getValueAndReset(int defaultCoounterValue){
        long count = counter.get();
        long val = value.get();
        Rate newRate;
        if (count !=0){
            newRate = new Rate(val,count);
        } else {
            newRate = new Rate(0,0);
        }
        value = new AtomicLong(0);
        counter = new AtomicLong(0);
        return newRate;
    }

}
