package com.rajsrinivas.common.concurrent.executor.task;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

class MetricsGatheringFutureTaskTest {

    @Test
    public void runWhenException() {
        new MetricsGatheringFutureTask( new Callable<Object>(){
            @Override
            public Object call() throws Exception {
                throw new Exception();
            }
        }).run();
        // No exception raised, caught by implementation of MetricsGatheringFutureTask
    }

}