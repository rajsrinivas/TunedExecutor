package com.rajsrinivas.common.concurrent.traffic;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This fixture is used to generate random numbers as inputs used to replay various scenarios
 * to compare different approaches.
 */
public class RecordableRandomNumberIterator implements Iterator<Double> {
    ConcurrentLinkedDeque<Double> recording = new ConcurrentLinkedDeque();
    public Iterator<Double> getRecording() { return recording.iterator();}

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Double next() {
        double random = Math.random();
        recording.add(random);
        return random;
    }
}
