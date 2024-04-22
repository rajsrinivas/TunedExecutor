package com.rajsrinivas.common.concurrent.eventrate.stat;

import com.rajsrinivas.common.concurrent.eventrate.Rate;

/**
 * Interface to retrieve vaues from different calculators
 */
public interface StatProvider {
    Rate getMostRecentStat();
}
