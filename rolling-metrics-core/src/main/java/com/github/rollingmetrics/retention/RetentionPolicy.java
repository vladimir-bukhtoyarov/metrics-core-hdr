/*
 *    Copyright 2017 Vladimir Bukhtoyarov
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.github.rollingmetrics.retention;

import java.time.Duration;

/**
 * TODO javadocs
 */
public interface RetentionPolicy {

    static RetentionPolicy uniform() {
        return UniformRetentionPolicy.INSTANCE;
    }

    static RetentionPolicy resetOnSnapshot() {
        return ResetOnSnapshotRetentionPolicy.INSTANCE;
    }

    static RetentionPolicy resetOnCondition(ResetCondition condition) {
        return new ResetOnConditionRetentionPolicy();
    }

    static RetentionPolicy resetPeriodically(Duration resettingPeriod) {
        return new ResetPeriodicallyRetentionPolicy(resettingPeriod);
    }

    static RetentionPolicy resetPeriodicallyByChunks(Duration rollingTimeWindow, int numberChunks) {
        return new ResetPeriodicallyByChunksRetentionPolicy(numberChunks, rollingTimeWindow);
    }

}