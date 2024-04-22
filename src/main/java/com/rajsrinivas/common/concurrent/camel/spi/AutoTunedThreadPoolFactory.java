package com.rajsrinivas.common.concurrent.camel.spi;

import com.rajsrinivas.common.concurrent.executor.NamedThreadFactory;
import com.rajsrinivas.common.concurrent.executor.TimedThreadPoolExecutor;
import com.rajsrinivas.common.concurrent.executor.parameters.ExecutorParameters;
import com.google.common.base.Optional;
import org.apache.camel.spi.ThreadPoolFactory;
import org.apache.camel.spi.ThreadPoolProfile;
import org.apache.camel.util.concurrent.RejectableScheduledThreadPoolExecutor;
import org.apache.camel.util.concurrent.RejectableThreadPoolExecutor;
import org.apache.camel.util.concurrent.SizedScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.*;

/**
 * <p>
 *     ThreadPoolFactory is camel interface that plugs implementations used by Camel framework
 *     It is already a spring component so integration into client needing to use this class is a simple
 *     component scan of this package.
 * </p>
 */

@Component
public class AutoTunedThreadPoolFactory implements ThreadPoolFactory {

    @Value ("{$autoTunedThreadPoolFactory.autoTune}")
    private boolean autoTune = false;

    @Value("{$autoTunedThreadPoolFactory.tuneFrequencyInSeconds}")
    int checkFrequencyInSeconds = 5;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public AutoTunedThreadPoolFactory() {log.info("Constructed " + this.getClass().getName());}

    @Override
    public ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return Executors.newCachedThreadPool(threadFactory);
    }

    @Override
    public ExecutorService newThreadPool(ThreadPoolProfile profile, ThreadFactory threadFactory) {
        boolean allow = Optional.fromNullable(profile.getAllowCoreThreadTimeOut()).or(false);
        return this.newThreadPool(profile.getPoolSize().intValue(),
                profile.getMaxPoolSize().intValue(), profile.getKeepAliveTime().longValue(),
                profile.getTimeUnit(), profile.getMaxQueueSize().intValue(),
                allow, profile.getRejectedExecutionHandler(), threadFactory);
    }

    public ExecutorService newThreadPool(int corePoolSize, int maxPoolSize,
                                          long keepAliveTime,
                                          TimeUnit timeUnit,
                                          int maxQueueSize, boolean allowCoreThreadTimeout,
                                          RejectedExecutionHandler rejectedExecutionHandler,
                                          ThreadFactory threadFactory) {
        if (corePoolSize < 0){
            throw new IllegalArgumentException( "CorePoolSize must be >= 0, was " + corePoolSize);
        } else if (maxPoolSize < corePoolSize){
            throw new IllegalArgumentException( "MaxPoolSize must be >= CorePoolSize , was " +
                    maxPoolSize + "<=" + corePoolSize);
        } else {
            BlockingQueue<Runnable> workQueue;
            if (corePoolSize == 0 && maxQueueSize <= 0){
                workQueue = new SynchronousQueue<>();
                corePoolSize = 1;
                maxPoolSize = 1;
            } else if ( maxPoolSize <=0 ){
                workQueue = new SynchronousQueue<>();
            } else {
                workQueue = new LinkedBlockingDeque<>(maxQueueSize);
            }
            if (corePoolSize == 1 && maxPoolSize == 1){
                RejectableThreadPoolExecutor rejectableThreadPoolExecutor = new RejectableThreadPoolExecutor(corePoolSize, maxPoolSize,
                        keepAliveTime, timeUnit, workQueue);
                rejectableThreadPoolExecutor.setThreadFactory(threadFactory);
                rejectableThreadPoolExecutor.allowCoreThreadTimeOut(allowCoreThreadTimeout);
                if (rejectedExecutionHandler == null){
                    rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
                }
                rejectableThreadPoolExecutor.setRejectedExecutionHandler(rejectedExecutionHandler);
                return rejectableThreadPoolExecutor;
            }
            BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();
            log.info("Returning timedThreadPoolExecutor");
            return new TimedThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime,
                    ExecutorParameters.TIME_UNIT_FOR_TIMEOUT,
                    queue, new NamedThreadFactory(), checkFrequencyInSeconds,
                    autoTune);
        }


    }

    @Override
    public ScheduledExecutorService newScheduledThreadPool(ThreadPoolProfile profile, ThreadFactory threadFactory) {
        Object rejectedExecutionHandler = profile.getRejectedExecutionHandler();
        if (rejectedExecutionHandler == null){
            rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        }
        RejectableScheduledThreadPoolExecutor answer = new RejectableScheduledThreadPoolExecutor(
                profile.getPoolSize().intValue(), threadFactory, (RejectedExecutionHandler)  rejectedExecutionHandler);
        answer.setRemoveOnCancelPolicy(true);
        return profile.getMaxPoolSize().intValue() > 0
                ? new SizedScheduledExecutorService(answer, profile.getMaxQueueSize().intValue())
                : answer;
    }
}
