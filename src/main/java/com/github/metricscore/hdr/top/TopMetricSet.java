/*
 *
 *  Copyright 2016 Vladimir Bukhtoyarov
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

package com.github.metricscore.hdr.top;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TopMetricSet implements MetricSet {

    private final BigDecimal zero;
    private final Map<String, Metric> gauges;

    public TopMetricSet(String name, Top top, TimeUnit latencyUnit, int digitsAfterDecimalPoint) {
        if (name == null) {
            throw new IllegalArgumentException("name should not be null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name should not be empty");
        }
        if (top == null) {
            throw new IllegalArgumentException("top should not be null");
        }
        if (latencyUnit == null) {
            throw new IllegalArgumentException("latencyUnit should not be null");
        }
        if (digitsAfterDecimalPoint < 0) {
            throw new IllegalArgumentException("digitsAfterDecimalPoint should not be negative");
        }

        gauges = new HashMap<>();
        gauges.put(name + ".latencyUnit", (Gauge<String>) latencyUnit::toString);

        zero = BigDecimal.ZERO.setScale(digitsAfterDecimalPoint, RoundingMode.CEILING);

        int size = top.getSize();
        for (int i = 0; i < size; i++) {
            String latencyName = name + "." + i + "." + "latency";
            Gauge<BigDecimal> latencyGauge = createLatencyGauge(i, top, latencyUnit, digitsAfterDecimalPoint);
            gauges.put(latencyName, latencyGauge);

            String descriptionName = name + "." + i + "." + "description";
            Gauge<String> descriptionGauge = createDescriptionGauge(i, top);
            gauges.put(descriptionName, descriptionGauge);
        }
    }

    @Override
    public Map<String, Metric> getMetrics() {
        return gauges;
    }

    private Gauge<BigDecimal> createLatencyGauge(int i, Top top, TimeUnit latencyUnit, int digitsAfterDecimalPoint) {
        return () -> {
            List<Position> positions = top.getPositionsInDescendingOrder();
            if (positions.size() <= i) {
                return zero;
            }
            double latencyNanos = positions.get(i).getLatencyInNanoseconds();
            long scale = latencyUnit.toNanos(1);
            double result = latencyNanos/scale;

            return new BigDecimal(result).setScale(digitsAfterDecimalPoint, RoundingMode.CEILING);
        };
    }

    private Gauge<String> createDescriptionGauge(int i, Top top) {
        return () -> {
            List<Position> positions = top.getPositionsInDescendingOrder();
            if (positions.size() <= i) {
                return "";
            }
            return positions.get(i).getQueryDescription();
        };
    }

}