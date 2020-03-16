/*
 *
 *  Copyright 2020 Vladimir Bukhtoyarov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.github.rollingmetrics.ranking;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ResetOnSnapshotRankingBenchmark {

    @State(Scope.Benchmark)
    public static class RankingState {

        private static final int TEST_DATA_COUNT = 1024 * 32;

        final String[] identities = new String[TEST_DATA_COUNT];

        final Ranking<String> resetOnSnapshotRanking_10 = Ranking.builder(10)
                .resetAllPositionsOnSnapshot()
                .withSnapshotCachingDuration(Duration.ZERO)
                .build();

        @Setup
        public void setup() {
            for (int i = 0; i < TEST_DATA_COUNT; i++) {
                identities[i] = i + "";
            }
        }
    }

    @Group("resetOnSnapshotTop_10")
    @GroupThreads(3)
    @Benchmark
    public void update_resetOnSnapshotRanking_10(RankingState state) {
        state.resetOnSnapshotRanking_10.update(getRandomValue(), state.identities[getRandomValue()]);
    }

    @Group("resetOnSnapshotTop_10")
    @GroupThreads(1)
    @Benchmark
    public List<Position> getSnapshot_resetOnSnapshotRanking_10(RankingState state) {
        return state.resetOnSnapshotRanking_10.getPositionsInDescendingOrder();
    }

    private static int getRandomValue() {
        return ThreadLocalRandom.current().nextInt(RankingState.TEST_DATA_COUNT);
    }

    public static class FourThread {
        public static void main(String[] args) throws RunnerException {
            Options opt = new OptionsBuilder()
                    .include(".*" + ResetOnSnapshotRankingBenchmark.class.getSimpleName() + ".*")
                    .warmupIterations(3)
                    .warmupTime(TimeValue.seconds(2))
                    .measurementIterations(3)
                    .threads(4)
                    .forks(1)
                    .build();
            try {
                new Runner(opt).run();
            } catch (RunnerException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
