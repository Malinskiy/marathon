package com.malinskiy.marathon.report.junit.model

/**
 * @param testsuites If only a single testsuite element is present, the testsuites element can be omitted. All attributes are optional.
 *
 * @param time time in seconds to execute all test suites
 * @param tests total number of tests from all testsuites. Some software may expect to only see the number of successful tests from all testsuites though
 * @param failures total number of failed tests from all testsuites
 * @param errors total number of tests with error result from all testsuites
 */
data class JUnitReport(
    val testsuites: List<TestSuite>,

    val name: String? = null,
    val time: String? = null,
    val tests: Int? = null,
    val failures: Int? = null,
    val disabled: Int? = null,
    val errors: Int? = null,
)
