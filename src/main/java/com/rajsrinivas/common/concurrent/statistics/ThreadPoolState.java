package com.rajsrinivas.common.concurrent.statistics;

public enum ThreadPoolState {
    ARRIVAL_RATE_AND_WAIT_TIME_INCREASED,
    ARRIVAL_RATE_INCREASED,
    WAIT_TIME_INCREASED,
    SYSTEM_STEADY;

    public static ThreadPoolState to_System_State(boolean arrivalRateIncreased,
                                                  boolean waitTimeIncreased){
        if (arrivalRateIncreased && waitTimeIncreased) return ARRIVAL_RATE_AND_WAIT_TIME_INCREASED;
        if (arrivalRateIncreased) return ARRIVAL_RATE_INCREASED;
        if (waitTimeIncreased) return WAIT_TIME_INCREASED;
        return ThreadPoolState.SYSTEM_STEADY;
    }
}
