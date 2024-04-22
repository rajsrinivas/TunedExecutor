package com.rajsrinivas.common.concurrent.eventrate;

import com.rajsrinivas.common.concurrent.eventrate.stat.AverageCalculator;
import com.rajsrinivas.common.concurrent.eventrate.stat.CountPerSecondCalculator;
import com.rajsrinivas.common.concurrent.eventrate.stat.ResetableStatCalculator;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Timer;

import static org.hamcrest.MatcherAssert.assertThat;

class SingleIntervalEventRateTimerTaskTest {

    @Test
    public void testAverageCalculator(){
        ResetableStatCalculator statCalculator = new AverageCalculator();
        SingleIntervalEventRateTimerTask task =
                new SingleIntervalEventRateTimerTask(new Timer(), 15, statCalculator);
        int[] delays = new int[] {7,2,3};
        for (int each : delays) {
            long sleepTime = each;
            sleep(sleepTime);
            task.accumulate(sleepTime);
        }
        sleep(4);
        assertThat(task.getMostRecentStat().getRate(), Matchers.is(4.0));
        sleep(15);
        assertThat(task.getMostRecentStat().getRate(), Matchers.is(0.0));
    }

    @Test
    public void testCountCalculation(){
        ResetableStatCalculator statCalculator = new CountPerSecondCalculator();
        SingleIntervalEventRateTimerTask task =
                new SingleIntervalEventRateTimerTask(new Timer(), 10, statCalculator);
        int[] delays = new int[] { 1,3, 3,2,3};
        for(int each : delays){
            task.accumulate(1);
            sleep(each/2);//int division
            task.accumulate(1);
            sleep(each - (each/2));
        }
        assertThat(task.getMostRecentStat().getRate(), Matchers.is(0.9));
        sleep(15);
        assertThat(task.getMostRecentStat().getRate(), Matchers.is(0.1));

    }

    private static void sleep(long sleepTime){
        try{
            Thread.sleep(sleepTime* 1000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

}