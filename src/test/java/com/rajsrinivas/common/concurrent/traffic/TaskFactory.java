package com.rajsrinivas.common.concurrent.traffic;

import java.util.Iterator;

public class TaskFactory implements  Iterator<Runnable>{
    private Iterator<Double> doubleIterator;
    public TaskFactory(Iterator<Double> iterator) { this.doubleIterator = iterator; }


    @Override
    public boolean hasNext() {
        return doubleIterator.hasNext();
    }

    @Override
    public Runnable next() {
        Double random = doubleIterator.next();
        int index = (int) (random * 10) % 2;
        int param = (int) (random * 10);
        switch(index){
            case 0:
                return new DiskWriteTask(param);
            default:
                return new SleepTask(param);
        }
    }
}
