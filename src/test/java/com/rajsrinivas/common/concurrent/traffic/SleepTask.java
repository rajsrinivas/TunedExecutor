package com.rajsrinivas.common.concurrent.traffic;

public class SleepTask implements Runnable{

    private final int sleepTime;
    public SleepTask(int sleepTime) { this.sleepTime = sleepTime; }
    public SleepTask() { sleepTime = (int) (Math.random() * 1000);}


    @Override
    public void run() {
        try{
            Thread.sleep(sleepTime);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}
