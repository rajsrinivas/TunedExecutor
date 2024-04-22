package com.rajsrinivas.common.concurrent.traffic;

import java.util.concurrent.ThreadPoolExecutor;

public class TrafficSimulator {
    private final ThreadPoolExecutor threadPoolExecutor;
    private final int parallelRuns;
    private final int durationInSeconds;
    private final int[] requestsPerSecondArray;

    public TrafficSimulator(ThreadPoolExecutor threadPoolExecutor, int[] requestsPerSecond,
                            int parallelRuns, int durationInSeconds){
        for(int each : requestsPerSecond){
            if (each > 1000)
                throw new IllegalArgumentException("Requests per second implemented only for milli resolution");
        }
        this.threadPoolExecutor = threadPoolExecutor;
        this.parallelRuns = parallelRuns;
        this.durationInSeconds = durationInSeconds;
        this.requestsPerSecondArray = requestsPerSecond;
    }

    /**
     * Traffic generator that submits tasks , current implementation is bursty, we could follow a distribution later.
     * @param tasks
     */
    public void simulate(TaskFactory tasks){
        Runnable runnable = () -> {
            for (int requestsPerSecond : requestsPerSecondArray) {
                for (int j = 0; j < durationInSeconds; j++) {
                    long nanoStart = System.nanoTime();
                    for (int i = 0; i < requestsPerSecond; i++) {
                        if (tasks.hasNext()) {
                            threadPoolExecutor.submit(tasks.next());
                        } else {
                            throw new IllegalArgumentException(" Task factory unexpectedly has no more tasks");
                        }
                    }
                    long nanoEnd = System.nanoTime();
                    try {
                        long wait = 1000 - ((nanoEnd - nanoStart) / (1000 * 1000));
                        if (wait > 0)
                            Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread[] threadsToJoin = new Thread[parallelRuns];
        for(int i =0; i < parallelRuns;i++){
            Thread eventGenerator = new Thread(runnable);
            eventGenerator.start();
            threadsToJoin[i] = eventGenerator;
        }
        for (int i = 0; i < parallelRuns ; i++){
            silentJoin(threadsToJoin[i]);
        }

    }

    private void silentJoin(Thread eventGenerator) {
        try{
            eventGenerator.join(durationInSeconds * 1000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

}
