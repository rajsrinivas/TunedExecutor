package com.rajsrinivas.common.concurrent.eventrate.stat;

import com.rajsrinivas.common.concurrent.eventrate.Rate;

/**
 * Interface that accumulates value and returns a {@link Rate} object, resetting totals
 * on every call to {@link #getValueAndReset(int)}
 */
public interface ResetableStatCalculator {

    void accumulate(long value);
    Rate getValueAndReset(int defaultCounterValue);
}
