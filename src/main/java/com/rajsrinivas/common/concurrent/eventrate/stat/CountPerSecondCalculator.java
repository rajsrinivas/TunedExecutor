package com.rajsrinivas.common.concurrent.eventrate.stat;

import com.rajsrinivas.common.concurrent.eventrate.Rate;

import java.util.concurrent.atomic.AtomicLong;

public class CountPerSecondCalculator implements ResetableStatCalculator {

    public static Factory factory = new Factory();
    private AtomicLong counter = new AtomicLong(0);

    private static class Factory implements CalculatorFactory {
        public CountPerSecondCalculator getInstance() {return new CountPerSecondCalculator();}
    }
    @Override
    public void accumulate(long value) {
        counter.addAndGet(value);
    }

    @Override
    public Rate getValueAndReset(int defaultCounterValue) {
        double count = counter.get();
        counter = new AtomicLong(0);
        return new Rate(count, defaultCounterValue);
    }
}
