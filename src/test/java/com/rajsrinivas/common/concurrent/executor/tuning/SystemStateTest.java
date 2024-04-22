package com.rajsrinivas.common.concurrent.executor.tuning;

import com.rajsrinivas.common.concurrent.statistics.ThreadPoolState;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThat;


public class SystemStateTest {
    @Test
    public void to_System_State() {
        assertThat(ThreadPoolState.to_System_State(true, true),
                Matchers.is(ThreadPoolState.ARRIVAL_RATE_AND_WAIT_TIME_INCREASED));
        assertThat(ThreadPoolState.to_System_State(true, false),
                Matchers.is(ThreadPoolState.ARRIVAL_RATE_INCREASED));
        assertThat(ThreadPoolState.to_System_State(false, true),
                Matchers.is(ThreadPoolState.WAIT_TIME_INCREASED));
        assertThat(ThreadPoolState.to_System_State(false, false),
                Matchers.is(ThreadPoolState.SYSTEM_STEADY));
    }
}
