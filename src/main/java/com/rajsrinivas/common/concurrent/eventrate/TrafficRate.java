package com.rajsrinivas.common.concurrent.eventrate;

import org.springframework.beans.factory.annotation.Value;

/**
 * A Container class that holds two {@link Rate} objects that denote a short term
 * and long ter=m window.
 */
public class TrafficRate {
    @Value ("${autoTunedThreadPoolFactory.shortTermStatIntervalInSeconds}")
    public static int SHORT_TERM_CHECK_IN_SECONDS = 1;
    @Value ("${autoTunedThreadPoolFactory.longTermStatIntervalInSeconds}")
    public static int LONG_TERM_CHECK_IN_SECONDS = 2;

    private final Rate shortTerm;
    private final Rate longTerm;

    public TrafficRate(Rate shortTerm, Rate longTerm) {
        this.shortTerm = shortTerm;
        this.longTerm = longTerm;
    }


    public Rate getShortTerm() {
        return shortTerm;
    }

    public Rate getLongTerm() {
        return longTerm;
    }





}
