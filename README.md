# Autotuned Executor


# Motivation
Typically, web applications have multiple thread pools and each has different usage characteristics. General approach is to set core pool size, max pool size and queue capacity. However,this is premised on traffic patterns that are based on average over days or months or simply gut feel, setting these parameters optimally is a challenge and at times plain wrong.For instance, bursty traffic has no good answer. So here is an attempt to monitor traffic in real time and tune these parameters for efficient use of thread resources.

# Description
Project to monitor active traffic and thread usage to auto tune Java executor threadpool parameters. 
This is useful to prioritize different thread pools within a JVM continuously monitoring traffice patterns.. 
Ideally, this should take away the burden of knowing traffic patterns for each thread pool takes away guesstimating parameters for each thread pool. It is suboptimal for a given point in time.
#Overview
Builds on Java Executor framework
    
Parameters are tuned based on Little's law(https://en.wikipedia.org/wiki/Little%27s_law). Premised on statistics of traffic patterns, arrival rate, process time.

    CorePoolSize : readily available threads
    MaxPoolSize : >= CorePoolSize such that thread growth overhead does not impact overall performance
    Executor Queue : A thread is created only when coresize is not reached or Queue is full and max size is not reached
In other words, if coresize is reached, task is tabled until queue is full after which thread is created till max pool size and task at head is processed. We are attempting to set these parameters by monitoring traffic from data collected over time windows viz short and long term time durations.

# High level idea
1. Start with unbounded queue, sacrifice latency for availability.
2. Measure an empirical throughput over time windows(For example, Past 1 second( deemed short term) , 10 seconds( deemed long term) or overall (from jvm start upto now). 
   - Essentially, gives nature of traffic, constant, bursty, uniform distribution.
      - Measurement components in moving window(rate calculators in code)
        - Arrival rate
        - Avg clock time to finish tasks. Is processing time increasing?
        - Avg wait times. Are requests growing in queue?
calling them **_arrival rate, process rate and wait rate_** respectively.
        
3. Use different settings to improve based on rates from #2 above
   1. Increase coresize and maxsize when high traffic
   2. Decrease queue size when low traffic
   3. On low load, decrease core and max size and keep alive time.
   4. Do nothing in constant or uniform distribution
   
# Approach details
      Given Arrival rate(per sec) & process time, wait times(in seconds)
         if 'a' is the arrival rate and 'pct' is the process clock time then a/pct ~= max through put
         By Little's theorem, # of inflight unprocessed requests = a * pct - count of threads. These get queued
   
Strategies to dial up and down threadpool parameters based on above statistics.***Note that we don't think in terms of cores or memory of the system, just arrival, process and wait times to reflect current system state.***

   **Dial up phase**

      Example:If we have 'a' = 10 req per second, and avg processing time is 0.5 sec
      ideal throughput should be 1/ 0.5 = 2 request per second per thread
      ideal queue capacity would then be 8, lets call this overload(its overload for current state of system)
      
      Given we can process 2 req per thread, question is, how we want to tackle overload 
      and provide reasonable through put. 

      Options: 
         We could get aggressive and increase coresize and 
         or Conservative would be 50% - 4
         or pessimistic 25%
         or Optimistic 75%
         or sure, max size by overload i.e. 100% - 8
   
   **Dial down phase**


      If 'a' < coresize Reduce coresize 
         Aggressive approach reduce queue size to zero
         Middle ground binary back-off
         Conservative - constant amortize percentage over time.
# Status
- Feature complete - 97.2 % code coverage with unit and end-to-end simiulation test(ThroughputTunerTest)
- Test with integration into camel, simulated 100000 requests from 100 threads (SimpleHttpClient)
- Added generated java doc
- TODO : Multilevel support for stats gathering, currently two windows are hardcoded. Add more unit tests.
- TODO : Externalize how aggressive the growth of pool parameters must be depending on OS and architecture, there seems to be lot of variations
- TODO : Clean up disk writes after simulation of IO tasks
- TODO : Server grade evaluation
- TODO : Clerical fixes like magic numbers, renaming methods and describing states and their context
# Where to start?
https://github.com/rajsrinivas/TunedExecutor/blob/master/src/test/java/com/rajsrinivas/common/concurrent/executor/tuning/ThroughputTunerSimulation.java does end to test with simulation of tasks. This is the top down entry point.
Sample output running on Mac, mileage varies on various OS & other architectures.
Running simulatation on untuned and auto tuned thread pools

    Stats gathered short term every 1 seconds, Long term every 2 seconds
    Initial pool settings core : 50 Max: 100 Keep alive time: 10
    Simulating Request: Batches of (10,50,250,80,500,0,50,100,350) by 3 parallel threads generate requests for 2 seconds.

         
        Running untuned thread pool(Scroll right to see thread counts)
            Legend: () --> Long term rates
            #   Arrival Rate        Process Rate        Process Time             Wait Time                Core      Current   Queue     Active    State
            1    30.00(30.00)        30.00(30.00)        25.40(74.32)             0.50(0.67)               50        50        17        50        SYSTEM_STEADY       
            2    240.00(750.00)      186.00(181.00)      274.75(198.19)           2101.17(638.35)          50        50        1350      50        WAIT_TIME_INCREASED
            3    0.00(0.00)          177.00(194.00)      278.56(268.54)           5967.50(5525.85)         50        50        3555      50        SYSTEM_STEADY       
            4    1050.00(300.00)     189.00(177.50)      271.08(281.33)           8209.60(6971.97)         50        50        5509      50        ARRIVAL_RATE_INCREASED
            5    0.00(0.00)          161.00(150.00)      303.27(325.84)           13136.85(12655.61)       50        50        4700      50        SYSTEM_STEADY       
            6    0.00(0.00)          161.00(192.50)      297.16(263.27)           17131.11(15730.11)       50        50        3838      50        SYSTEM_STEADY       
            7    0.00(0.00)          167.00(172.50)      274.68(273.76)           22133.62(21648.17)       50        50        3037      50        SYSTEM_STEADY       
            8    0.00(0.00)          145.00(155.50)      341.82(317.18)           21410.83(20766.02)       50        50        2233      50        SYSTEM_STEADY       
            9    0.00(0.00)          91.00(100.50)       530.78(470.23)           24956.36(24427.95)       50        50        1650      50        SYSTEM_STEADY       
            10   0.00(0.00)          145.00(72.50)       565.13(690.15)           29968.01(28234.05)       50        50        1172      50        SYSTEM_STEADY       
            11   0.00(0.00)          171.00(186.50)      303.56(272.15)           34133.45(33624.63)       50        50        221       50        SYSTEM_STEADY       
            12   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               50        50        0         0         SYSTEM_STEADY       
            13   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               50        50        0         0         SYSTEM_STEADY       
            14   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               50        50        0         0         SYSTEM_STEADY       
            15   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               50        50        0         0         SYSTEM_STEADY       
            16   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               50        50        0         0         SYSTEM_STEADY       
            17   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               50        50        0         0         SYSTEM_STEADY       
            18   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               50        50        0         0         SYSTEM_STEADY       
            19   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               50        50        0         0         SYSTEM_STEADY       
            Service statistics
            Arrival Rate:
            Total: 8340
            Completion rate:
            Total: 8340
            Task Statistics:
            Net wait time :
            Count: 8340.0 Total: 134562404
            Net process time:
            Count: 8340.0 Total: 2551689
            

Replaying same traffic pattern on auto tuned thread pool
Legend: () --> Long term rates
         Scroll right to see thread counts
         
         #   Arrival Rate        Process Rate        Process Time             Wait Time                Core      Current   Queue     Active    State     
        1    30.00(30.00)        30.00(30.00)        28.60(26.05)             0.00(0.17)               50        50        15        50        SYSTEM_STEADY       
        2    240.00(750.00)      182.00(207.00)      316.82(275.14)           2178.08(572.04)          53        53        1345      53        WAIT_TIME_INCREASED
        3    0.00(0.00)          183.00(164.00)      299.75(315.89)           6152.97(5688.98)         53        53        3648      53        SYSTEM_STEADY       
        4    1050.00(300.00)     162.00(170.50)      324.67(313.59)           8257.86(7336.55)         53        53        5604      53        ARRIVAL_RATE_INCREASED
        5    0.00(0.00)          339.00(480.00)      1154.24(1050.91)         11403.35(10969.57)       500       500       3055      500       SYSTEM_STEADY       
        6    0.00(0.00)          270.00(343.50)      2105.09(1531.07)         10792.30(10574.38)       500       500       1642      500       SYSTEM_STEADY       
        7    0.00(0.00)          210.00(193.00)      1520.14(1498.94)         13251.84(12955.41)       500       500       99        500       SYSTEM_STEADY       
        8    0.00(0.00)          113.00(71.00)       7276.68(6094.65)         12649.99(12412.35)       524       524       0         192       SYSTEM_STEADY       
        9    0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               524       524       0         0         SYSTEM_STEADY       
        10   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               131       131       0         0         SYSTEM_STEADY       
        11   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               32        32        0         0         SYSTEM_STEADY       
        12   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               8         8         0         0         SYSTEM_STEADY       
        13   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               8         8         0         0         SYSTEM_STEADY       
        14   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               8         8         0         0         SYSTEM_STEADY       
        15   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               8         8         0         0         SYSTEM_STEADY       
        16   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               8         8         0         0         SYSTEM_STEADY       
        17   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               8         8         0         0         SYSTEM_STEADY       
        18   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               8         8         0         0         SYSTEM_STEADY       
        19   0.00(0.00)          0.00(0.00)          0.00(0.00)               0.00(0.00)               8         8         0         0         SYSTEM_STEADY       
        Service statistics
        Arrival Rate:
        Total: 8340
        Completion rate:
        Total: 8340
        Task Statistics:
        Net wait time :
        Count: 8340.0 Total: 73735615
        Net process time:
        Count: 8340.0 Total: 10456065
        Scroll right to see thread counts

**Observation :** 
        
    Throughput
            We can see that when threadpool count is 50 in the first run, it took 11 iterations and when dialed up to 500 threads processing finished in 7th iteration. 
    Resource utilization:
            Also , after no traffic , threadpool size is decremented.
