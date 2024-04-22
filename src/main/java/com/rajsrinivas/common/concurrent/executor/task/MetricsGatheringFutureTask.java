package com.rajsrinivas.common.concurrent.executor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsGatheringFutureTask<V> extends FutureTask<V> implements MetricProvider {

    private static final Logger logger = LoggerFactory.getLogger(MetricsGatheringFutureTask.class);
    private long createTime;
    private long start;
    private long elapsed;
    private final String name;
    private static AtomicLong id = new AtomicLong();
    private String threadName;
    private Callable<V> callable;

    public MetricsGatheringFutureTask(Callable<V> callable) {
        super(callable);
        this.callable = callable;
        this.name = String.valueOf(id.incrementAndGet());
        createTime = System.nanoTime();
    }

    public MetricsGatheringFutureTask(Runnable runnable, V arg) { this(Executors.callable(runnable, arg));}

    @Override
    public long getWaitTime() {
        return (start - createTime) / 1000000;
    }

    @Override
    public void run() {
        startEvent();
        threadName = Thread.currentThread().getName();
        try{
            super.run();
            super.get(); // This would log exception
        }catch(Exception t){
            t.printStackTrace();
        } finally {
            finishEvent();
        }
    }

    private void finishEvent() {
        long end = System.nanoTime();
        this.elapsed = end - start;
    }

    private void startEvent() {
        start = System.nanoTime();
    }
    @Override
    public long getProcessTime() {return elapsed / (1000000);}// convert to seconds

    public String getName() { return threadName +":" + name;}
    public Callable<V> getCallable() { return callable;}

}
