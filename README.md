# Metrics core HDR
This library is addressed to provide tightly integration of [HdrHistogram](https://github.com/HdrHistogram/HdrHistogram) into [Metrics Core](https://dropwizard.github.io/metrics/3.1.0/manual/core/) as first class citizen.
Do not waste your time to figure out who is better in the fight of metrics-core versus HdrHistogram, just use two solutions together.

From the HdrHistogram, you get:
* Min/max loss-less capturing, in opposite to built-in Metrics Core reservoir implementations which can loss critical min/max values because of its sampling nature. 
* Non-blocking recording(short path optimization). It is not possible to come in situation when any writer is blocked by snapshot extraction.
* Garbage free recording. When reservoir increased up to its max size then it stops memory allocation and starts reusing already allocated memory, as result no garbage flows from new to old generation. 

From the Metrics Core, you get:
* Useful, consistent, and extendable API. Metrics Core library is so popular because of its well-designed API. 
* Integration(out of the box and third-parties) with hundreds libraries, servers, frameworks and API stacks.
* Integration(out of the box and third-parties) with many monitoring systems like graphite, ganglia, influxdb.

`Metrics core HDR` provides ability to take advantages from both powerful libraries.

## Build status
[![Build Status](https://travis-ci.org/vladimir-bukhtoyarov/metrics-core-hdr.svg?branch=master)](https://travis-ci.org/vladimir-bukhtoyarov/metrics-core-hdr)
[![Coverage Status](https://coveralls.io/repos/github/vladimir-bukhtoyarov/metrics-core-hdr/badge.svg?branch=master)](https://coveralls.io/github/vladimir-bukhtoyarov/metrics-core-hdr?branch=master)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://api.bintray.com/packages/vladimir-bukhtoyarov/maven/metrics-core-hdr/images/download.svg) ](https://bintray.com/vladimir-bukhtoyarov/maven/metrics-core-hdr/_latestVersion)
[![Join the chat at https://gitter.im/vladimir-bukhtoyarov/metrics-core-hdr](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/vladimir-bukhtoyarov/metrics-core-hdr?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Get Metrics-Core-HDR library

#### By direct link
[Download compiled jar, sources, javadocs](https://github.com/vladimir-bukhtoyarov/metrics-core-hdr/releases/tag/1.1.0)

#### You can build Metrics-Core-HDR from sources

```bash
git clone https://github.com/vladimir-bukhtoyarov/metrics-core-hdr.git
cd metrics-core-hdr
mvn clean install
```

#### You can add Metrics-Core-HDR to your project as maven dependency

The Metrics-Core-HDR library is distributed through [Bintray](http://bintray.com/), so you need to add Bintray repository to your `pom.xml`

```xml
     <repositories>
         <repository>
             <id>jcenter</id>
             <url>http://jcenter.bintray.com</url>
         </repository>
     </repositories>
```

Then include Metrics-Core-HDR as dependency to your `pom.xml`

```xml
<dependency>
    <groupId>com.github.metrics-core-addons</groupId>
    <artifactId>metrics-core-hdr</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Usage
### Metrics construction and registration
In order to construct metrics you need to create [builder](https://github.com/vladimir-bukhtoyarov/metrics-core-hdr/blob/master/src/main/java/com/github/metricscore/hdrhistogram/HdrBuilder.java) instance. 
The one builder instance can be reused to construct metrics multiple times. 

#### Example of timer construction

```java
  HdrBuilder builder = HdrBuilder();

  // Build and register timer in one line of code. 
  // Prefer this style by default.
  Timer timer1 = builder.buildAndRegisterTimer(registry, "my-timer-1");

  // Build and register timer in two lines of code.
  // Use this style if you plan to register timer in multiple registers
  Timer timer2 = builder.buildTimer();
  registry1.register(timer2, "my-timer-2");
  
  // build and register timer in three lines of code(verbose way). Most likely you never need in this.
  Reservoir reservoir = builder.buildReservoir();
  Timer timer3 = new Timer(reservoir);
  registry.register(timer3, "my-timer-3");
```

#### Example of histogram construction
```java
  HdrBuilder builder = HdrBuilder();

  // build and register histogram in one line of code (prefer this style)
  Histogram histogram1 = builder.buildAndRegisterHistogram(registry, "my-histogram-1");

  // Build and register timer in two lines of code.
  // Use this style if you plan to register histogram in multiple registers
  Histogram histogram2 = builder.buildHistogram();
  registry.register(histogram2, "my-histogram-2");
  
  // build and register histogram in three lines of code(verbose way). Most likely you never need in this.
  Reservoir reservoir = builder.buildReservoir();
  Histogram histogram3 = new Histogram(reservoir);
  registry.register(histogram3, "my-timer-3");
```

## Basic configuration options
This section describes basic configuration options supported by [HdrBuilder API](https://github.com/vladimir-bukhtoyarov/metrics-core-hdr/blob/master/src/main/java/com/github/metricscore/hdrhistogram/HdrBuilder.java).   
#### Number of significant value digits
This option configures the number of significant decimal digits to which the histogram will maintain value resolution and separation.
Value precision is expressed as the number of significant digits in the value recording, and provides control over value quantization behavior across the value range and the subsequent value resolution at any given level.
```java
  builder.withSignificantDigits(3);
```
When <tt>numberOfSignificantValueDigits</tt> is not configured then default value <tt>2</tt> will be applied.
if something still unclear about this option then refer directly to HdrHistogram [documentation](https://github.com/HdrHistogram/HdrHistogram) and [sources](https://github.com/HdrHistogram/HdrHistogram/blob/master/src/main/java/org/HdrHistogram/AbstractHistogram.java).
#### Lowest discernible value
Configures the lowest value that can be discerned. 
Providing a <tt>lowestDiscernibleValue</tt> is useful is situations where the units used for the histogram's values are much smaller that the minimal accuracy required. 
E.g. when tracking time values stated in nanosecond units, where the minimal accuracy required is a microsecond, the proper value for <tt>lowestDiscernibleValue</tt> would be <tt>1000</tt>.
```java
  builder.withLowestDiscernibleValue(1000);  
```
There is no default value for <tt>lowestDiscernibleValue</tt>, when it is not specified then it will not be applied.
if something still unclear about this option then refer directly to HdrHistogram [documentation](https://github.com/HdrHistogram/HdrHistogram) and [sources](https://github.com/HdrHistogram/HdrHistogram/blob/master/src/main/java/org/HdrHistogram/AbstractHistogram.java).

#### Highest trackable value
Configures the highest value to be tracked by the histogram. Providing this option can help to reduce memory footprint. 
When you are specifying <tt>highestTrackableValue</tt> then you also must specify behavior which should be applied when writing to reservoir value which greater than <tt>highestTrackableValue</tt>  
```java
  // Values which higher than 3600000 for example 3979684 will be reduced to 3600000 before recording into histogram 
  builder.withHighestTrackableValue(3600000L, OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE);  
  
  // Values which higher than 3600000 for example 3979684 will not be written to histogram 
  builder.withHighestTrackableValue(3600000L, OverflowResolver.SKIP);
  
  // Values which higher than 3600000 for example 3979684 will be written to histogram as is, as result ArrayIndexOutOfBoundsException may be thrown.
  // Use this way with double attention iff you clearly understanding what you do.
  builder.withHighestTrackableValue(3600000L, OverflowResolver.PASS_THRU);
```
There is no default value for <tt>highestTrackableValue</tt>, when it is not specified then it will not be applied.
if something still unclear about this option then refer directly to HdrHistogram [documentation](https://github.com/HdrHistogram/HdrHistogram) and [sources](https://github.com/HdrHistogram/HdrHistogram/blob/master/src/main/java/org/HdrHistogram/AbstractHistogram.java).

#### Predefined percentiles 
This option configures list of percentiles which you plan to obtain from each snapshot. 
If you already know what kind of percentiles you plan to measure, then it would be better to specify it to optimize snapshot size, 
as result unnecessary garbage will be avoided, memory for snapshot will be allocated only for percentiles which you configured.
Moreover by default builder already configured with default list of percentiles <tt>double[] {0.5, 0.75, 0.9, 0.95, 0.98, 0.99, 0.999}</tt>.
```java
  builder.withPredefinedPercentiles(new double[] {0.5, 0.6, 0.7, 0.75, 0.8, 0.9, 0.95, 0.96, 0.97, 0.98, 0.99, 0.999});  
```
If you do not know concrete percentiles which you need and default is not enough then you can discard snapshot optimization.
When snapshot footprint is unoptimized then snapshot becomes as accurate as it is supported by underlying histogram, but memory required for take one snapshot will approximately equals to histogram size.
```java
  builder.withoutSnapshotOptimization();  
```

## Configuration options for evicting the old values of from reservoir.
HdrHistogram do not lose recorded values, it is good because you do not lose min/max values, 
but in same time it is bad because in real world use-cases you need to show measurements which actual to current moment of time or time window,
nobody interests in percentiles aggregated for a few days or weeks, everybody wants to see percentiles which actual now. 
So you need in way to deleted obsolete(already not interested) values from reservoir, Metrics-Core-Hdr provides four different strategies to do this:

#### Reset reservoir on snapshot
Reservoir configured with this strategy will be cleared each time when snapshot taken.
```java
  builder.resetReservoirOnSnapshot();  
```
This strategy provides guarantee that data once reported will never reported again. But this strategy is bad for case of multiple reporters(for example graphite, JMX and CSV log) because reporters will steal data from each other.  

#### Reset reservoir periodically
Reservoir configured with this strategy will be cleared fully after each `resettingPeriod`.
```java
  // reset the reservoir at whole every five minutes
  builder.resetReservoirPeriodically(Duration.ofMinutes(5));  
```
This strategy is compatible with case of multiple reporters, but in same time it is not provides guaranties that any value written to reservoir will not be reported twice,
also at least once reporting semantic also does not supported, because you can miss something value when resetting reservoir immediately after the value was written.

#### Reset reservoir by chunks
Reservoir configured with this strategy will be divided to <tt>numberChunks</tt> parts, and one chunk will be cleared after each <tt>resettingPeriod</tt>.
The measure written to reservoir will take affect to the snapshot at least <tt>resettingPeriod * (numberChunks - 1)</tt> and at most <tt>resettingPeriod * numberChunks</tt> time.
```java
  // Split reservoir by 7 chunks, each measure written to reservoir will take affect to the snapshot approximately 60-70 seconds
  builder.resetReservoirByChunks(Duration.ofSeconds(10), 7);  
```
This strategy is more smoothly then <tt>resetReservoirPeriodically</tt> because reservoir never zeroyed at whole, so user experience provided by <tt>resetReservoirByChunks</tt> should look more pretty.
But remember about memory footprint and do not split reservoir to big amount of chunks. The maximum possible value for <tt>numberChunks</tt> is 25. 

#### Never reset
This strategy should be used if you want to store in reservoir all values since reservoir creation, in other words eviction is not needed.
```java
  builder.neverResetReservoir();  
```
This is default behavior, you should not configure anything to achieve it.

## Advanced features
This section describes miscellaneous options which are not proposed to be used for regular use cases.  

#### Coordinated omission
When this option is configured then it will be used to compensate for the loss of sampled values when a recorded value is larger than the expected interval between value samples,
Histogram will auto-generate an additional series of decreasingly-smaller (down to the expectedIntervalBetweenValueSamples) value records.
```java
  builder.withExpectedIntervalBetweenValueSamples(10);  
```
**WARNING:** You should not use this feature for monitoring your application in the production, its designed to be used inside benchmarks and load testing.
if something still unclear about this option then refer directly to HdrHistogram [documentation](https://github.com/HdrHistogram/HdrHistogram) and [sources](https://github.com/HdrHistogram/HdrHistogram/blob/master/src/main/java/org/HdrHistogram/AbstractHistogram.java).
    
#### Snapshot caching 
This option configures the period for which taken snapshot will be cached. Snapshot caching can be useful together with bad-designed monitoring solutions(like [Zabbix java gateway](https://www.zabbix.com/documentation/2.0/ru/manual/concepts/java)) which pull monitoring data from application through chain(like RMI/JMX) which does not allow to catch multiple values in single request,
as result the values which logically coupled(like different percentiles from same histogram) can be showed on the monitoring screens with unbelievable artifacts, 
for example "95 percentile" can be showed is greater then "99 percentile" because each of theme taken from different snapshots.
You can try to solve this problem by specifying snapshot caching period which greater then one poll cycle but is smaller then intervals between polls: 
```java
  // 3 seconds usually enough for single poll cycle 
  builder.withSnapshotCachingDuration(Duration.ofSeconds(3));  
```
**NOTE:** Snapshot caching is very useful to have deal with bad-designed monitoring databases, 
but it does not provide 100% guaranties that logically coupled data always will be stored in the database from same snapshot.

Have a question?
----------------
Feel free to ask in the [gitter chat](https://gitter.im/vladimir-bukhtoyarov/metrics-core-hdr)

License
-------
Copyright 2016 Vladimir Bukhtoyarov
Licensed under the Apache Software License, Version 2.0: <http://www.apache.org/licenses/LICENSE-2.0>.