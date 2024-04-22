package com.rajsrinivas.common.concurrent.executor.tuning;

import com.rajsrinivas.common.concurrent.executor.parameters.ExecutorParameters;
import com.rajsrinivas.common.concurrent.executor.parameters.TunableParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>
 *     A mediating TimerTask class that retrives current parameters , runs evaluation
 *     and sets new recommendation on passed in thread pool.
 * </p>
 */
public class ThroughputTuner extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(ThroughputTuner.class);
    private final ThreadPoolExecutor threadPoolExecutor;
    private boolean autotune = true;
    private ThreadPoolConfigurationRecommender configurationRecommender;

    public ThroughputTuner(ThreadPoolExecutor threadPoolExecutor , boolean autotune,
                           ThreadPoolConfigurationRecommender configurationRecommender){
        this.threadPoolExecutor = threadPoolExecutor;
        this.autotune = autotune;
        this.configurationRecommender = configurationRecommender;

    }

    @Override
    public void run(){
        try{
            ExecutorParameters executorParameters = getCurrentExecutorParameters();
            TunableParameters tunableParameters = configurationRecommender.getRecommendation(executorParameters);
            if(autotune){
                int corePoolSize = tunableParameters.getCorePoolSize();
                if(corePoolSize > 0 && corePoolSize <= threadPoolExecutor.getMaximumPoolSize())
                    threadPoolExecutor.setCorePoolSize(corePoolSize);
                int maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
                if(maxPoolSize > 0 && corePoolSize >= maxPoolSize){
                    threadPoolExecutor.setMaximumPoolSize(tunableParameters.getMaxPoolSize());
                }
                threadPoolExecutor.setKeepAliveTime(tunableParameters.getKeepAliveTime(),
                        ExecutorParameters.TIME_UNIT_FOR_TIMEOUT);
                threadPoolExecutor.prestartAllCoreThreads();
            }
        }catch(Exception t){
            logger.error("Error trying to tune threadpool , message :" + t.getMessage() );
            StringWriter sw = new StringWriter();
            t.printStackTrace();
            logger.info(sw.getBuffer().toString());
        }
    }

    private ExecutorParameters getCurrentExecutorParameters() {
        return new ExecutorParameters(threadPoolExecutor);
    }

}
