---
layout: docs
title: "Options"
category: doc
date: 2018-11-19 16:55:00
order: 3
---

Configuration for CLI is done using the YAML formatted configuration file which is placed by default in the root of the project and named as
 **Marathonfile**.

Below is an example of Marathonfile (without the vendor module configuration:

```yaml
name: "sample-app tests"
outputDir: "./marathon"
analyticsConfiguration:
  influx:
    url: "http://influx.svc.cluster.local:8086"
    user: "root"
    password: "root"
    dbName: "marathon"
poolingStrategy:
- type: "omni"
shardingStrategy:
  type: "count"
  count: 5
sortingStrategy:
  type: "success-rate"
  timeLimit: "2015-03-14T09:26:53.590Z"
batchingStrategy:
  type: "fixed-size"
  size: 5
flakinessStrategy:
  type: "probability"
  minSuccessRate: 0.7
  maxCount: 3
  timeLimit: "2015-03-14T09:26:53.590Z"
retryStrategy:
  type: "fixed-quota"
  totalAllowedRetryQuota: 100
  retryPerTestQuota: 3
```

Each of these options is covered in detail in the section below. If you're unsure how to properly format your options in Marathonfile take
 a look at the samples or take a look at the [deserialisation logic][5] in the *cli* module of the project. Each option might have a default
 deserialiser from yaml or a custom one. Usually the custom deserialiser expects the type option to understand which type of strategy we
 need to instantiate.

Configuration for gradle plugin is done via gradle only. It doesn't support the CLI's marathonfile.
Here is an example of gradle config using Kotlin DSL:

```kotlin
marathon {
    name = "sample-app tests"
    baseOutputDir = "./marathon"
    analytics {
        influx {
            url = "http://influx.svc.cluster.local:8086"
            user = "root"
            password = "root"
            dbName = "marathon"
        }
    }
    poolingStrategy {
        operatingSystem = true
    }
    shardingStrategy {
        countSharding {
            count = 5
        }
    }
    sortingStrategy {
        executionTime {
            percentile = 90.0
            executionTime = Instant.now().minus(3, ChronoUnit.DAYS)
        }
    }
    batchingStrategy {
        fixedSize {
            size = 10
        }
    }
    flakinessStrategy {
        probabilityBased {
            minSuccessRate = 0.8
            maxCount = 3
            timeLimit = Instant.now().minus(30, ChronoUnit.DAYS)
        }
    }
    retryStrategy {
        fixedQuota {
            totalAllowedRetryQuota = 200
            retryPerTestQuota = 3
        }
    }
    filteringConfiguration {
        allowlist {
            add(SimpleClassnameFilter(".*".toRegex()))
        }
        blocklist {
            add(SimpleClassnameFilter("$^".toRegex()))
        }
    }
    testClassRegexes = listOf("^((?!Abstract).)*Test$")
    includeSerialRegexes = emptyList()
    excludeSerialRegexes = emptyList()
    uncompletedTestRetryQuota = 100
    ignoreFailures = false
    isCodeCoverageEnabled = false
    fallbackToScreenshots = false
    testOutputTimeoutMillis = 30_000
    strictMode = false
    debug = true
    autoGrantPermission = true
}
```

Here you will find a list of currently supported configuration parameters and examples of how to set them up. Keep in mind that some of the
 additional parameters might not be supported by all vendor modules. If you find that something doesn't work - please submit an issue for a
 vendor module at fault.   

* TOC
{:toc}

# General parameters

## Test run configuration name
This string specifies the name of this test run configuration. It is used mainly in the generated test reports.

{% tabs name %}
{% tab name Marathonfile %}
```yaml
name: "My test run for sample app"
```
{% endtab %}
{% tab name Gradle %}
```kotlin
marathon {
    name = "My test run for sample app"
}
```
{% endtab %}
{% tab name Gradle Kotlin %}
```kotlin
marathon {
    name = "My test run for sample app"
}
```
{% endtab %}
{% endtabs %}

## Output directory
Directory path to use as the root folder for all the runner output (logs, reports, etc). 

For gradle, the output path will automatically be set to a `marathon` folder in your reports folder unless it's overridden.

{% tabs output-directory %}
{% tab output-directory Marathonfile %}
```yaml
outputDir: "build/reports/marathon"
```
{% endtab %}
{% tab output-directory Gradle %}
```kotlin
marathon {
    baseOutputDir = "some-path"
}
```
{% endtab %}
{% tab output-directory Gradle Kotlin %}
```kotlin
marathon {
    baseOutputDir = "some-path"
}
```
{% endtab %}
{% endtabs %}

# Analytics configuration
Configuration of analytics backend to be used for storing and retrieving test metrics. This plays a major part in optimising performance and mitigating flakiness.

## Disabled analytics
By default no analytics backend is expected which means that each test will be treated as a completely new test.

## [InfluxDB][1]
Assuming you've done the setup for InfluxDB you need to provide:
- url
- username
- password
- database name
- retention policy

Database name is quite useful in case you have multiple configurations of tests/devices and you don't want metrics from one configuration to affect the other one, e.g. regular and end-to-end tests.

{% tabs analytics-influxdb %}
{% tab analytics-influxdb Marathonfile %}
```yaml
analyticsConfiguration:
  influx:
    url: "http://influx.svc.cluster.local:8086"
    user: "root"
    password: "root"
    dbName: "marathon"
    retentionPolicyConfiguration:
      name: "rpMarathonTest"
      duration: "90d"
      shardDuration: "1h"
      replicationFactor: 5
      isDefault: false
```
{% endtab %}
{% tab analytics-influxdb Gradle %}
```kotlin
marathon {
    analytics {
        influx {
            url = "http://influx.svc.cluster.local:8086"
            user = "root"
            password = "root"
            dbName = "marathon"
        }
    }
}
```
{% endtab %}
{% tab analytics-influxdb Gradle Kotlin %}
```kotlin
marathon {
    analytics {
        influx {
            url = "http://influx.svc.cluster.local:8086"
            user = "root"
            password = "root"
            dbName = "marathon"
        }
    }
}
```
{% endtab %}
{% endtabs %}

## [Graphite][2]
Graphite can be used as an alternative to InfluxDB. It uses the following parameters:
- host
- port (optional) - the default is 2003
- prefix (optional) - no metrics prefix will be used if not specified

{% tabs analytics-graphite %}
{% tab analytics-graphite Marathonfile %}
```yaml
analyticsConfiguration:
  graphite:
    host: "influx.svc.cluster.local"
    port: "8080"
    prefix: "prf"
```
{% endtab %}
{% tab analytics-graphite Gradle %}
```kotlin
marathon {
    analytics {
        graphite {
            host = "influx.svc.cluster.local"
            port = "8080"
            prefix = "prf"
        }
    }
}
```
{% endtab %}
{% tab analytics-graphite Gradle Kotlin %}
```kotlin
marathon {
    analytics {
        graphite {
            host = "influx.svc.cluster.local"
            port = "8080"
            prefix = "prf"
        }
    }
}
```
{% endtab %}
{% endtabs %}

# Execution flow

## Pooling strategy
Pooling strategy affects how devices are grouped together.

### Omni a.k.a. one huge pool
All connected devices are merged into one group. **This is the default mode**.

{% tabs pooling-omni %}
{% tab pooling-omni Marathonfile %}
```yaml
poolingStrategy:
  - type: "omni"
```
{% endtab %}
{% tab pooling-omni Gradle %}
```kotlin
marathon {
    //Omni is the default strategy
    poolingStrategy {}
}
```
{% endtab %}
{% tab pooling-omni Gradle Kotlin %}
```kotlin
marathon {
    //Omni is the default strategy
    poolingStrategy {}
}
```
{% endtab %}
{% endtabs %}

### By abi
Devices are grouped by their ABI, e.g. *x86* and *mips*.

{% tabs pooling-abi %}
{% tab pooling-abi Marathonfile %}
```yaml
poolingStrategy:
  - type: "abi"
```
{% endtab %}
{% tab pooling-abi Gradle %}
```kotlin
marathon {
    poolingStrategy {
        abi = true
    }
}
```
{% endtab %}
{% tab pooling-abi Gradle Kotlin %}
```kotlin
marathon {
    poolingStrategy {
        abi = true
    }
}
```
{% endtab %}
{% endtabs %}

### By manufacturer
Devices are grouped by manufacturer, e.g. *Samsung* and *Yota*.

{% tabs pooling-manufacturer %}
{% tab pooling-manufacturer Marathonfile %}
```yaml
poolingStrategy:
  - type: "manufacturer"
```
{% endtab %}
{% tab pooling-manufacturer Gradle %}
```kotlin
marathon {
    poolingStrategy {
        manufacturer = true
    }
}
```
{% endtab %}
{% tab pooling-manufacturer Gradle Kotlin %}
```kotlin
marathon {
    poolingStrategy {
        manufacturer = true
    }
}
```
{% endtab %}
{% endtabs %}

### By device model
Devices are grouped by model name, e.g. *LG-D855* and *SM-N950F*.

{% tabs pooling-model %}
{% tab pooling-model Marathonfile %}
```yaml
poolingStrategy:
  - type: "device-model"
```
{% endtab %}
{% tab pooling-model Gradle %}
```kotlin
marathon {
    poolingStrategy {
        model = true
    }
}
```
{% endtab %}
{% tab pooling-model Gradle Kotlin %}
```kotlin
marathon {
    poolingStrategy {
        model = true
    }
}
```
{% endtab %}
{% endtabs %}

### By OS version
Devices are grouped by OS version, e.g. *24* and *25*.

{% tabs pooling-os %}
{% tab pooling-os Marathonfile %}
```yaml
poolingStrategy:
  - type: "os-version"
```
{% endtab %}
{% tab pooling-os Gradle %}
```kotlin
marathon {
    poolingStrategy {
        operatingSystem = true
    }
}
```
{% endtab %}
{% tab pooling-os Gradle Kotlin %}
```kotlin
marathon {
    poolingStrategy {
        operatingSystem = true
    }
}
```
{% endtab %}
{% endtabs %}

## Sharding strategy
Sharding is a mechanism that allows the marathon to affect the tests scheduled for execution inside each pool.

### Parallel sharding
Executes each test in parallel on all the available devices in pool. This is the default behaviour.

{% tabs sharding-parallel %}
{% tab sharding-parallel Marathonfile %}
```yaml
shardingStrategy:
  type: "parallel"
```
{% endtab %}
{% tab sharding-parallel Gradle %}
```kotlin
marathon {
    //Parallel is the default strategy
    shardingStrategy {}
}
```
{% endtab %}
{% tab sharding-parallel Gradle Kotlin %}
```kotlin
marathon {
    //Parallel is the default strategy
    shardingStrategy {}
}
```
{% endtab %}
{% endtabs %}

### Count sharding
Executes each test **count** times inside each pool. For example you want to test the flakiness of a specific test hence you need to execute
 this test a lot of times. Instead of running the build X times just use this sharding strategy and the test will be executed X times.

{% tabs sharding-count %}
{% tab sharding-count Marathonfile %}
```yaml
shardingStrategy:
  type: "count"
  count: 5
```
{% endtab %}
{% tab sharding-count Gradle %}
```kotlin
marathon {
    shardingStrategy {
        countSharding { 
            count = 5
        }
    }
}
```
{% endtab %}
{% tab sharding-count Gradle Kotlin %}
```kotlin
marathon {
    shardingStrategy {
        countSharding { 
            count = 5
        }
    }
}
```
{% endtab %}
{% endtabs %}

## Sorting strategy
In order to optimise the performance of test execution tests need to be sorted. 
This requires analytics backend enabled since we need historical data in order to anticipate tests behaviour like duration and 
success/failure rate.

### No sorting
No sorting of tests is done at all. This is the default behaviour.

{% tabs sorting-no %}
{% tab sorting-no Marathonfile %}
```yaml
sortingStrategy:
  type: "no-sorting"
```
{% endtab %}
{% tab sorting-no Gradle %}
```kotlin
marathon {
    sortingStrategy {}
}
```
{% endtab %}
{% tab sorting-no Gradle Kotlin %}
```kotlin
marathon {
    sortingStrategy {}
}
```
{% endtab %}
{% endtabs %}

### Success rate sorting
For each test analytics storage is providing the success rate for a time window specified by time **timeLimit** parameter. 
All the tests are then sorted by the success rate in an increasing order, that is failing tests go first and successful tests go last.
If you want to reverse the order set the `ascending` to `true`.

{% tabs sorting-success-rate %}
{% tab sorting-success-rate Marathonfile %}
```yaml
sortingStrategy:
  type: "success-rate"
  timeLimit: "2015-03-14T09:26:53.590Z"
  ascending: false
```
{% endtab %}
{% tab sorting-success-rate Gradle %}
```kotlin
marathon {
    sortingStrategy {
        successRate { 
            limit = Instant.now().minus(Duration.parse("PT1H"))
            ascending = false
        }
    }
}
```
{% endtab %}
{% tab sorting-success-rate Gradle Kotlin %}
```kotlin
marathon {
    sortingStrategy {
        successRate { 
            limit = Instant.now().minus(Duration.parse("PT1H"))
            ascending = false
        }
    }
}
```
{% endtab %}
{% endtabs %}

### Execution time sorting
For each test analytics storage is providing the X percentile duration for a time window specified by time **timeLimit** parameter. 
Apart from absolute date/time it  can be also be an ISO 8601 formatted duration.

Percentile is configurable via the **percentile** parameter. 

All the tests are sorted so that long tests go first and short tests are executed last. 
This allows marathon to minimise the error of balancing the execution of tests at the end of execution.

{% tabs sorting-execution-time %}
{% tab sorting-execution-time Marathonfile %}
```yaml
sortingStrategy:
  type: "execution-time"
  percentile: 80.0
  timeLimit: "-PT1H"
```
{% endtab %}
{% tab sorting-execution-time Gradle %}
```kotlin
marathon {
    sortingStrategy {
        executionTime { 
            percentile = 80.0
            timeLimit = Instant.now().minus(Duration.parse("PT1H"))
        }
    }
}
```
{% endtab %}
{% tab sorting-execution-time Gradle Kotlin %}
```kotlin
marathon {
    sortingStrategy {
        executionTime { 
            percentile = 80.0
            timeLimit = Instant.now().minus(Duration.parse("PT1H"))
        }
    }
}
```
{% endtab %}
{% endtabs %}

## Batching strategy
Batching mechanism allows you to trade off stability for performance. 
A group of tests executed using one single run is called a batch. 
Most of the times this means that between tests in the same batch you're sharing the device state so there is no clean-up. 
On the other hand you gain some performance improvements 
since the execution command usually is quite slow (up to 10 seconds for some platforms).

### Isolate batching
No batching is done at all, each test is executed using separate command execution, that is performance is sacrificed in favor of stability. 
This is the default mode.

{% tabs batching-isolated %}
{% tab batching-isolated Marathonfile %}
```yaml
batchingStrategy:
  type: "isolate"
```
{% endtab %}
{% tab batching-isolated Gradle %}
```kotlin
marathon {
    batchingStrategy {}
}
```
{% endtab %}
{% tab batching-isolated Gradle Kotlin %}
```kotlin
marathon {
    batchingStrategy {}
}
```
{% endtab %}
{% endtabs %}

### Fixed size batching
Each batch is created based on the **size** parameter which is required. 
When a new batch of tests is needed the queue is dequeued for at most **size** tests.

Optionally if you want to limit the batch duration you have to specify the **timeLimit** for the test metrics time window
and the **durationMillis**.  For each test the analytics backend is accessed and **percentile** of it's duration is queried. 
If the sum of durations is more than the **durationMillis** then no more tests are added to the batch.

This is useful if you have very very long tests and you use batching, e.g. you batch by size 10 and your test run duration is roughly
 10 minutes, but you have tests that are expected to run 2 minutes each. If you batch all of them together then at least one device will be
 finishing it's execution in 20 minutes while all other devices might already finish. 
 To mitigate this just specify the time limit for the batch using **durationMillis**.

Another optional parameter for this strategy is the **lastMileLength**. At the end of execution batching tests actually hurts the
 performance so for the last tests it's much better to execute them in parallel in separate batches. This works only if you execute on 
 multiple devices. You can specify when this optimisation kicks in using the **lastMileLength** parameter, the last **lastMileLength** tests
 will use this optimisation.

{% tabs batching-fixed %}
{% tab batching-fixed Marathonfile %}
```yaml
batchingStrategy:
  type: "fixed-size"
  size: 5
  durationMillis: 100000
  percentile: 80.0
  timeLimit: "-PT1H"
  lastMileLength: 10
```
{% endtab %}
{% tab batching-fixed Gradle %}
```kotlin
marathon {
    batchingStrategy {
        fixedSize { 
            size = 5
            durationMillis = 100000
            percentile = 80.0
            timeLimit = Instant.now().minus(Duration.parse("PT1H"))
            lastMileLength = 10
        }
    }
}
```
{% endtab %}
{% tab batching-fixed Gradle Kotlin %}
```kotlin
marathon {
    batchingStrategy {
        fixedSize { 
            size = 5
            durationMillis = 100000
            percentile = 80.0
            timeLimit = Instant.now().minus(Duration.parse("PT1H"))
            lastMileLength = 10
        }
    }
}
```
{% endtab %}
{% endtabs %}

## Flakiness strategy
This is the main anticipation logic for marathon. Using the analytics backend we can understand the success rate and hence queue preventive
 retries to mitigate the flakiness of the tests and environment.

### Ignore flakiness
Nothing is done with this mode. This is the default behaviour.

{% tabs flakiness-ignore %}
{% tab flakiness-ignore Marathonfile %}
```yaml
flakinessStrategy:
  type: "ignore"
```
{% endtab %}
{% tab flakiness-ignore Gradle %}
```kotlin
marathon {
    flakinessStrategy {}
}
```
{% endtab %}
{% tab flakiness-ignore Gradle Kotlin %}
```kotlin
marathon {
    flakinessStrategy {}
}
```
{% endtab %}
{% endtabs %}

### Probability based flakiness strategy
The main idea is that flakiness strategy anticipates the flakiness of the test based on the probability of test passing and tries to
 maximise the probability of passing when executed multiple times. For example the probability of test A passing is 0.5 and configuration
 has probability of 0.8 requested, then the flakiness strategy multiplies the test A to be executed 3 times (0.5 x 0.5 x 0.5 = 0.125 is
 the probability of all tests failing, so with probability 0.875 > 0.8 at least one of tests will pass).

The minimal probability that you want is specified using **minSuccessRate** during the time window controlled by the **timeLimit**.
 Additionally if you specify too high **minSuccessRate** you'll have too many retries, so the upper bound for this is controlled by the
 **maxCount** parameter so that this strategy will calculate the required number of retries according to the **minSuccessRate** but if it's
 higher than the **maxCount** it will choose **maxCount**.

{% tabs flakiness-probability %}
{% tab flakiness-probability Marathonfile %}
```yaml
flakinessStrategy:
  type: "probability"
  minSuccessRate: 0.7
  maxCount: 3
  timeLimit: "2015-03-14T09:26:53.590Z"
```
{% endtab %}
{% tab flakiness-probability Gradle %}
```kotlin
marathon {
    flakinessStrategy {
        probabilityBased { 
            minSuccessRate = 0.7
            maxCount = 3
            timeLimit = Instant.now().minus(Duration.parse("PT1H"))
        }
    }
}
```
{% endtab %}
{% tab flakiness-probability Gradle Kotlin %}
```kotlin
marathon {
    flakinessStrategy {
        probabilityBased { 
            minSuccessRate = 0.7
            maxCount = 3
            timeLimit = Instant.now().minus(Duration.parse("PT1H"))
        }
    }
}
```
{% endtab %}
{% endtabs %}

## Retry strategy
This is the logic that kicks in if our preventive logic failed to anticipate such high number of retries. This works after the tests were
 actually executed.

### No retries
As the name implies, no retries are done. This is the default mode.

{% tabs retry-no %}
{% tab retry-no Marathonfile %}
```yaml
retryStrategy:
  type: "no-retry"
```
{% endtab %}
{% tab retry-no Gradle %}
```kotlin
marathon {
    retryStrategy {}
}
```
{% endtab %}
{% tab retry-no Gradle Kotlin %}
```kotlin
marathon {
    retryStrategy {}
}
```
{% endtab %}
{% endtabs %}

### Fixed quota retry strategy
Parameter **totalAllowedRetryQuota** specifies how many retries at all (for all the tests is total) are allowed. **retryPerTestQuota**
 controls how many retries can be done for each test individually.

{% tabs retry-fixed %}
{% tab retry-fixed Marathonfile %}
```yaml
retryStrategy:
  type: "fixed-quota"
  totalAllowedRetryQuota: 100
  retryPerTestQuota: 3
```
{% endtab %}
{% tab retry-fixed Gradle %}
```kotlin
marathon {
    retryStrategy {
        fixedQuota { 
            retryPerTestQuota = 3
            totalAllowedRetryQuota = 100
        }
    }
}
```
{% endtab %}
{% tab retry-fixed Gradle Kotlin %}
```kotlin
marathon {
    retryStrategy {
        fixedQuota { 
            retryPerTestQuota = 3
            totalAllowedRetryQuota = 100
        }
    }
}
```
{% endtab %}
{% endtabs %}

# Additional parameters

## Test filtering configuration
Filtering of tests is important since usually we as developers have the same codebase for all the different types of tests we want to
 execute. In order to indicate to marathon which tests you want to execute you can use the allowlist and blocklist parameters. First
 allowlist is applied, then the blocklist. Each accepts a *TestFilter* based on the *class name*, *fully qualified class name*, *package*, 
 *annotation* or *method*. Each expects a regular expression as a value.

In order to filter using multiple filters at the same time a *composition* filter is also available which accepts a list of base filters and
 also an operation such as **UNION**, **INTERSECTION** or **SUBTRACT**. You can create complex filters such as get all the tests
 starting with *E2E* but get only methods from there ending with *Test*. Composition filter is not supported by groovy gradle scripts, but 
 is supported if you use gradle kts.

An important thing to mention is that by default platform specific ignore options are not taken into account. This is because a
 cross-platform test runner cannot account for all the possible test frameworks out there. However, each framework's ignore option can still
 be "explained" to marathon, e.g. JUnit's **org.junit.Ignore** annotation can be specified in the filtering configuration.

{% tabs filtering %}
{% tab filtering Marathonfile %}
```yaml
filteringConfiguration:
  allowlist:
    - type: "simple-class-name"
      regex: ".*"
    - type: "fully-qualified-class-name"
      regex: ".*"
    - type: "method"
      regex: ".*"
    - type: "composition"
      filters:
        - type: "package"
          regex: ".*"
        - type: "method"
          regex: ".*"
      op: "UNION"
  blocklist:
    - type: "package"
      regex: ".*"
    - type: "annotation"
      regex: ".*"
```
{% endtab %}
{% tab filtering Gradle %}
```kotlin
marathon {
    filteringConfiguration {
        allowlist {
            simpleClassNameFilter = [".*"]
            fullyQualifiedClassnameFilter = [".*"]
            testMethodFilter = [".*"]
        }
        blocklist {
            testPackageFilter = [".*"]
            annotationFilter = [".*"]
        }
    }
}
```
{% endtab %}
{% tab filtering Gradle Kotlin %}
```kotlin
marathon {
    filteringConfiguration { 
        allowlist = listOf(
            SimpleClassnameFilter(".*".toRegex()),
            FullyQualifiedClassnameFilter(".*".toRegex()),
            TestMethodFilter(".*".toRegex()),
            CompositionFilter(
                listOf(
                    TestPackageFilter(".*".toRegex()),
                    TestMethodFilter(".*".toRegex())
                ),
                CompositionFilter.OPERATION.UNION
            )
        )
        blocklist = listOf(
            TestPackageFilter(".*".toRegex()),
            AnnotationFilter(".*".toRegex())
        )
    }
}
```
{% endtab %}
{% endtabs %}

## Test class regular expression
By default, test classes are found using the ```"^((?!Abstract).)*Test[s]*$"``` regex. You can override this if you need to.

{% tabs test-class-regex %}
{% tab test-class-regex Marathonfile %}
```yaml
testClassRegexes:
  - "^((?!Abstract).)*Test[s]*$"
```
{% endtab %}
{% tab test-class-regex Gradle %}
```kotlin
marathon {
    testClassRegexes = [
        "^((?!Abstract).)*Test[s]*\$"
    ]
}
```
{% endtab %}
{% tab test-class-regex Gradle Kotlin %}
```kotlin
marathon {
    testClassRegexes = listOf(
        "^((?!Abstract).)*Test[s]*$"
    )
}
```
{% endtab %}
{% endtabs %}

## Ignore failures
By default, the build fails if some tests failed. If you want to the build to succeed even if some tests failed use *true*.

{% tabs ignore-failures %}
{% tab ignore-failures Marathonfile %}
```yaml
ignoreFailures: true
```

{% endtab %} {% tab ignore-failures Gradle %}

```kotlin
marathon {
    ignoreFailures = true
}
```

{% endtab %} {% tab ignore-failures Gradle Kotlin %}

```kotlin
marathon {
    ignoreFailures = true
}
```

{% endtab %} {% endtabs %}

## Code coverage

Depending on the vendor implementation code coverage may not be supported. By default, code coverage is disabled. If this option is enabled,
code coverage will be collected and marathon assumes that code coverage generation will be setup by user (e.g. proper build flags, jacoco
jar added to classpath, etc).

{% tabs code-coverage %} {% tab code-coverage Marathonfile %}

```yaml
isCodeCoverageEnabled: true
```

{% endtab %} {% tab code-coverage Gradle %}

```kotlin
marathon {
    isCodeCoverageEnabled = true
}
```

{% endtab %} {% tab code-coverage Gradle Kotlin %}

```kotlin
marathon {
    isCodeCoverageEnabled = true
}
```

{% endtab %} {% endtabs %}

## Test output timeout

This parameter specifies the behaviour for the underlying test executor to timeout if there is no output. By default, this is set to 5
minutes.

{% tabs test-output-timeout %} {% tab test-output-timeout Marathonfile %}

```yaml
testOutputTimeoutMillis: 30000
```

{% endtab %}
{% tab test-output-timeout Gradle %}
```kotlin
marathon {
    testOutputTimeoutMillis = 30000
}
```
{% endtab %}
{% tab test-output-timeout Gradle Kotlin %}
```kotlin
marathon {
    testOutputTimeoutMillis = 30000
}
```
{% endtab %}
{% endtabs %}

## Test batch timeout

This parameter specifies the behaviour for the underlying test executor to timeout if the batch execution exceeded some duration. By
default, this is set to 30 minutes.

{% tabs test-output-timeout %} {% tab test-output-timeout Marathonfile %}
```yaml
testBatchTimeoutMillis: 900000
```
{% endtab %}
{% tab test-output-timeout Gradle %}
```kotlin
marathon {
    testBatchTimeoutMillis = 900000
}
```
{% endtab %}
{% tab test-output-timeout Gradle Kotlin %}
```kotlin
marathon {
    testBatchTimeoutMillis = 900000
}
```
{% endtab %}
{% endtabs %}

## Device provider init timeout
When the test run starts device provider is expected to provide some devices. This should not take more than 3 minutes by default. If your
 setup requires this to be changed please override as following: 

{% tabs device-provider-init-timeout %}
{% tab device-provider-init-timeout Marathonfile %}
```yaml
deviceInitializationTimeoutMillis: 300000
```
{% endtab %}
{% tab device-provider-init-timeout Gradle %}
```kotlin
marathon {
    deviceInitializationTimeoutMillis = 300000
}
```
{% endtab %}
{% tab device-provider-init-timeout Gradle Kotlin %}
```kotlin
marathon {
    deviceInitializationTimeoutMillis = 300000
}
```
{% endtab %}
{% endtabs %}

## Analytics tracking
To better understand the use-cases that marathon is used for we're asking you to provide us with anonymised information about your usage.
 By default, this is disabled. Use **true** to enable.

{% tabs analytics-tracking %}
{% tab analytics-tracking Marathonfile %}
```yaml
analyticsTracking: true
```
{% endtab %}
{% tab analytics-tracking Gradle %}
```kotlin
marathon {
    analyticsTracking = true
}
```
{% endtab %}
{% tab analytics-tracking Gradle Kotlin %}
```kotlin
marathon {
    analyticsTracking = true
}
```
{% endtab %}
{% endtabs %}

## Uncompleted test retry quota
By default, tests that don't have any status reported after execution (for example a device disconnected during the execution) retry
 indefinitely. You can limit the number of total execution for such cases using this option.

{% tabs uncompleted-test-retry-quote %}
{% tab uncompleted-test-retry-quote Marathonfile %}
```yaml
uncompletedTestRetryQuota: 100
```
{% endtab %}
{% tab uncompleted-test-retry-quote Gradle %}
```kotlin
marathon {
    uncompletedTestRetryQuota = 100
}
```
{% endtab %}
{% tab uncompleted-test-retry-quote Gradle Kotlin %}
```kotlin
marathon {
    uncompletedTestRetryQuota = 100
}
```
{% endtab %}
{% endtabs %}

## Strict mode
By default, if one of the test retries succeeds then the test is considered successfully executed. If you require success status only when
 all retries were executed successfully you can enable the strict mode. This may be useful to verify that flakiness of tests was fixed for
 example.
 
{% tabs strict-mode %}
{% tab strict-mode Marathonfile %}
```yaml
strictMode: true
```
{% endtab %}
{% tab strict-mode Gradle %}
```kotlin
marathon {
    strictMode = true
}
```
{% endtab %}
{% tab strict-mode Gradle Kotlin %}
```kotlin
marathon {
    strictMode = true
}
```
{% endtab %}
{% endtabs %}

## Debug mode
Enabled very verbose logging to stdout of all the marathon components. Very useful for debugging.

{% tabs debug-mode %}
{% tab debug-mode Marathonfile %}
```yaml
debug: true
```
{% endtab %}
{% tab debug-mode Gradle %}
```kotlin
marathon {
    debug = true
}
```
{% endtab %}
{% tab debug-mode Gradle Kotlin %}
```kotlin
marathon {
    debug = true
}
```
{% endtab %}
{% endtabs %}

## Screen recording policy
By default, screen recording will only be pulled for tests that failed (**ON_FAILURE** option). This is to save space and also to reduce the
 test duration time since we're not pulling additional files. If you need to save screen recording regardless of the test pass/failure 
 please use the **ON_ANY** option:

{% tabs screen-recording-policy %}
{% tab screen-recording-policy Marathonfile %}
```yaml
screenRecordingPolicy: "ON_ANY"
```
{% endtab %}
{% tab screen-recording-policy Gradle %}
```kotlin
marathon {
    screenRecordingPolicy = com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy.ON_ANY
}
```
{% endtab %}
{% tab screen-recording-policy Gradle Kotlin %}
```kotlin
marathon {
    screenRecordingPolicy = com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy.ON_ANY
}
```
{% endtab %}
{% endtabs %}

# Vendor configuration
See relevant vendor module page, e.g. [Android][3] or [iOS][4]

[1]: https://www.influxdata.com/
[2]: https://graphiteapp.org/
[3]: {% post_url 2018-11-19-android %}
[4]: {% post_url 2018-11-19-ios %}
[5]: https://github.com/MarathonLabs/marathon/blob/develop/cli/src/main/kotlin/com/malinskiy/marathon/cli/config/ConfigFactory.kt
