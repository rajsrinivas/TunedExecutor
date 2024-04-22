package com.rajsrinivas.common.concurrent.eventrate;

public class Rate {

    private final double value;
    private final double count;

    public Rate(double value, double count){
        this.value = value;
        this.count = count;
    }
    public double getValue() {return value;}
    public double getCount() { return count;}
    public double getRate() { return count > 0 ? value /count : count;}
}
