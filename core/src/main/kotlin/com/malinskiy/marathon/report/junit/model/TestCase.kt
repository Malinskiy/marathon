package com.malinskiy.marathon.report.junit.model

/**
 * @param name Name of the test method, required
 * @param time Time taken (in seconds) to execute the test. optional
 * @param classname Full class name for the class the test method is in. required
 *
 * @param skipped skipped can appear 0 or once. optional
 * @param error error indicates that the test errored. An errored test had an unanticipated problem. For example an unchecked throwable (exception), crash or a problem with the implementation of the test. Contains as a text node relevant data for the error, for example a stack trace. optional
 * @param failure failure indicates that the test failed. A failure is a condition which the code has explicitly failed by using the mechanisms for that purpose. For example via an assertEquals. Contains as a text node relevant data for the failure, e.g., a stack trace. optional
 *
 * @param systemOut Data that was written to standard out while the test was executed. optional
 * @param systemErr Data that was written to standard error while the test was executed. optional
 */
data class TestCase(
    val name: String,
    val time: String? = null,
    val classname: String,
    val group: String? = null,

    /**
     * If the test was not executed or failed, you can specify one of the skipped, error or failure elements
     */
    val skipped: Skipped? = null,
    val error: Error? = null,
    val failure: Failure? = null,

    val rerunFailure: List<Rerun> = emptyList(),
    val rerunError: List<Rerun> = emptyList(),
    val flakyFailure: List<Rerun> = emptyList(),
    val flakyError: List<Rerun> = emptyList(),

    val systemOut: String? = null,
    val systemErr: String? = null,
)
