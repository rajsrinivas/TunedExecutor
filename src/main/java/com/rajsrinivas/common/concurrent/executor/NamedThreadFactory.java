package com.rajsrinivas.common.concurrent.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class NamedThreadFactory implements ThreadFactory {
    private AtomicLong id = new AtomicLong();

    @Override
    public Thread newThread(Runnable runnable){
        Thread t = new Thread(runnable);
        t.setDaemon(false);
        t.setName("#"+id.incrementAndGet());
        return t;
    }
}
