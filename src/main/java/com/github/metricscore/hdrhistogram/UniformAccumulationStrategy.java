package com.github.metricscore.hdrhistogram;

import com.codahale.metrics.Snapshot;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

class UniformAccumulationStrategy implements AccumulationStrategy {

    public static UniformAccumulationStrategy INSTANCE = new UniformAccumulationStrategy();

    private UniformAccumulationStrategy() {}

    @Override
    public Accumulator createAccumulator(Recorder recorder) {
        return new UniformAccumulator(recorder);
    }

    private static class UniformAccumulator implements Accumulator {
        private final Lock lock = new ReentrantLock();
        private final Recorder recorder;
        final Histogram uniformHistogram ;
        private Histogram intervalHistogram;

        public UniformAccumulator(Recorder recorder) {
            this.recorder = recorder;
            this.intervalHistogram = recorder.getIntervalHistogram();
            this.uniformHistogram = intervalHistogram.copy();
        }

        @Override
        public void recordValue(long value) {
            recorder.recordValue(value);
        }

        @Override
        public Snapshot getSnapshot(Function<Histogram, Snapshot> snapshotTaker) {
            lock.lock();
            try {
                intervalHistogram = recorder.getIntervalHistogram(intervalHistogram);
                uniformHistogram.add(intervalHistogram);
                return snapshotTaker.apply(uniformHistogram);
            } finally {
                lock.unlock();
            }
        }
    }

}
