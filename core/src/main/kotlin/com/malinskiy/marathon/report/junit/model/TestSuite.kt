package com.malinskiy.marathon.report.junit.model

/**
 * @param name Full (class) name of the test for non-aggregated testsuite documents. Class name without the package for aggregated testsuites documents. Required
 * @param tests The total number of tests in the suite, required
 * @param errors The total number of tests in the suite that errored. An errored test is one that had an unanticipated problem, for example an unchecked throwable; or a problem with the implementation of the test. optional
 * @param failures The total number of tests in the suite that failed. A failure is a test which the code has explicitly failed by using the mechanisms for that purpose. e.g., via an assertEquals. optional
 * @param hostname Host on which the tests were executed. 'localhost' should be used if the hostname cannot be determined. optional
 * @param id Starts at 0 for the first testsuite and is incremented by 1 for each following testsuite. optional
 * @param pkg Derived from testsuite/@name in the non-aggregated documents. optional
 * @param skipped The total number of skipped tests. optional
 * @param time Time taken (in seconds) to execute the tests in the suite. optional
 * @param timestamp when the test was executed in ISO 8601 format (2014-01-21T16:17:18). Timezone may not be specified. optional
 *
 * @param properties Properties (e.g., environment settings) set during test execution
 * @param testcase testcase can appear multiple times
 * @param systemOut Data that was written to standard out while the test suite was executed. optional
 * @param systemErr Data that was written to standard error while the test suite was executed. optional
 */
data class TestSuite(
    val name: String,
    val tests: Int,
    val failures: Int,
    val errors: Int,

    val group: String? = null,
    val time: String? = null,
    val skipped: Int? = null,
    val timestamp: String? = null,
    val hostname: String? = null,
    val id: String? = null,
    val pkg: String? = null,
    val file: String? = null,
    val log: String? = null,
    val url: String? = null,
    val version: String? = null,

    val properties: List<Property> = emptyList(),
    val testcase: List<TestCase> = emptyList(),

    val systemOut: String? = null,
    val systemErr: String? = null,
)
