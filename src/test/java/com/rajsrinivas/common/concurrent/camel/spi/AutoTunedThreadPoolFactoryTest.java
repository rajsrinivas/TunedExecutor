package com.rajsrinivas.common.concurrent.camel.spi;

import com.rajsrinivas.common.concurrent.executor.TimedThreadPoolExecutor;
import org.apache.camel.spi.ThreadPoolProfile;
import org.apache.camel.util.concurrent.RejectableThreadPoolExecutor;
import org.apache.camel.util.concurrent.SizedScheduledExecutorService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;


import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


public class AutoTunedThreadPoolFactoryTest{

    private final AutoTunedThreadPoolFactory autoTunedThreadPoolFactory =
            new AutoTunedThreadPoolFactory();
    @Mock
    private ThreadFactory threadFactory;
    @Mock
    private ThreadPoolProfile threadPoolProfile;

    @BeforeEach
    public void setup(){
        openMocks(this);
        when(threadPoolProfile.getRejectedExecutionHandler()).thenReturn(null);
        when(threadPoolProfile.getMaxPoolSize()).thenReturn(100);
        when(threadPoolProfile.getPoolSize()).thenReturn(100);
        when(threadPoolProfile.getMaxQueueSize()).thenReturn(10);
        when(threadPoolProfile.getKeepAliveTime()).thenReturn(100L);
        when(threadPoolProfile.getTimeUnit()).thenReturn(TimeUnit.SECONDS);
        when(threadPoolProfile.getAllowCoreThreadTimeOut()).thenReturn(true);
    }

    @Test
    public void newCachedThreadPool(){
        assertThat(autoTunedThreadPoolFactory.newCachedThreadPool(threadFactory), Matchers.notNullValue());
    }

    @Test
    public void newScheduledThreadPool(){
        ScheduledExecutorService  scheduledExecutorService =
                autoTunedThreadPoolFactory.newScheduledThreadPool(threadPoolProfile, threadFactory);
        assertThat(scheduledExecutorService instanceof SizedScheduledExecutorService, Matchers.is(true));
    }

    @Test
    public void createThreadPoolFromProfile(){
        ExecutorService scheduledExecutorService =
                autoTunedThreadPoolFactory.newThreadPool(threadPoolProfile, threadFactory);
        assertThat(scheduledExecutorService instanceof TimedThreadPoolExecutor, Matchers.is(true));
    }

    @Test
    public void newThreadPoolWithoutProfileTimeoutTrue(){
        ExecutorService executor = autoTunedThreadPoolFactory.newThreadPool(1,1,10,TimeUnit.SECONDS,
        1,true,null, threadFactory);
        assertThat(executor instanceof RejectableThreadPoolExecutor, Matchers.is(true));
    }

    @Test
    public void newThreadPoolWithoutProfileTimeoutFalse(){
        ExecutorService executor = autoTunedThreadPoolFactory.newThreadPool(1,1,10,TimeUnit.SECONDS,
                1,false,null, threadFactory);
        assertThat(executor instanceof RejectableThreadPoolExecutor, Matchers.is(true));
    }

    @Test
    public void newThreadPoolWithoutProfileTimeoutWhenCorePoolSizeNegative(){
        assertThrows(IllegalArgumentException.class, () -> autoTunedThreadPoolFactory.newThreadPool(-1,1,10,TimeUnit.SECONDS,
                1,false,null, threadFactory));
    }

    @Test
    public void newThreadPoolWithoutProfileTimeoutWhenCorePoolSizeMoreThanMaxPoolSize(){
        assertThrows(IllegalArgumentException.class, () -> autoTunedThreadPoolFactory.newThreadPool(10,1,10,TimeUnit.SECONDS,
                1,false,null, threadFactory));
    }

    @Test
    public void newThreadPoolWithoutProfileTimeoutWhenMaxQueueSizeNegative(){
        assertThrows(IllegalArgumentException.class, () -> autoTunedThreadPoolFactory.newThreadPool(1,1,10,TimeUnit.SECONDS,
                -1,false,null, threadFactory));
    }
}