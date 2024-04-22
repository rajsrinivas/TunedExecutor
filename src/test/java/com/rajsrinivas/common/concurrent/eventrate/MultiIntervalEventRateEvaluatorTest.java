package com.rajsrinivas.common.concurrent.eventrate;

import com.rajsrinivas.common.concurrent.eventrate.stat.AverageCalculator;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;


import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;

class MultiIntervalEventRateEvaluatorTest {

    @Test
    public void testEndToEnd(){
        Logger logger = Logger.getLogger("MyLogger");
        setuplogging(logger);
        MultiIntervalEventRateEvaluator evaluator =
                new MultiIntervalEventRateEvaluator(new int[] {15,60}, AverageCalculator.factory);
        // For the first 15 seconds, 7, 2, 3 values are passed totalling 3 times so the
        // avg in first 15 seconds must  be (7+2+3)/3 = 4
        int[] delays = new int[] {7,2,3,9, 5, 3};
        for( int each : delays){
            sleep(each);
            evaluator.event(each);
        }
        TrafficRate trafficRate = evaluator.getRates();
        assertThat(trafficRate.getShortTerm().getRate(), Matchers.is(4.0));
        sleep(32);
        trafficRate = evaluator.getRates();
        // Short term
        assertThat(trafficRate.getShortTerm().getRate(), Matchers.is(0.0));
        // Long term
        assertThat(trafficRate.getLongTerm().getValue(), Matchers.is(29.0));
        assertThat(trafficRate.getLongTerm().getCount(), Matchers.is(6.0));
        assertThat(trafficRate.getLongTerm().getRate(), Matchers.is(29.0/6));
        evaluator.shutdown();
    }

    private static void sleep(long sleepTime){
        try{
            Thread.sleep(sleepTime* 1000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void setuplogging(Logger logger){
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
    }
}