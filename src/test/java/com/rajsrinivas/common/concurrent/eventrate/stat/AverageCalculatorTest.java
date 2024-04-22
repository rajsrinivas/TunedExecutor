package com.rajsrinivas.common.concurrent.eventrate.stat;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class AverageCalculatorTest {

    @Test
    public void testAverageCalculator() {
        ResetableStatCalculator avgCalc = new AverageCalculator();
        avgCalc.accumulate(10);
        avgCalc.accumulate(7);
        MatcherAssert.assertThat(avgCalc.getValueAndReset(10).getRate(), Matchers.is(8.5));
        avgCalc.accumulate(3);
        MatcherAssert.assertThat(avgCalc.getValueAndReset(10).getRate(), Matchers.is(3.0));
    }

}