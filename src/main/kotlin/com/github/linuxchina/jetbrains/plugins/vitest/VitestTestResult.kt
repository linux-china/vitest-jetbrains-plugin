package com.github.linuxchina.jetbrains.plugins.vitest


class VitestTestResult {
    var numTotalTestSuites: Int? = null
    var numPassedTestSuites: Int? = null
    var numFailedTestSuites: Int? = null
    var numPendingTestSuites: Int? = null
    var numTotalTests: Int? = null
    var numPassedTests: Int? = null
    var numFailedTests: Int? = null
    var numPendingTests: Int? = null
    var numTodoTests: Int? = null
    var startTime: Long? = null
    var success: Boolean? = true
    var testResults: List<TestResult>? = null

    fun findTestResult(filePath: String, testName: String): AssertionResult? {
        testResults?.forEach { testResult ->
            if (testResult.name == filePath) {
                testResult.assertionResults?.forEach { assertionResult ->
                    if (assertionResult.title == testName) {
                        assertionResult.startTime = testResult.startTime
                        return assertionResult
                    }
                }
            }
        }
        return null
    }
}

class TestResult {
    var startTime: Long? = null
    var endTime: Long? = null
    var status: String? = null
    var message: String? = null
    var name: String? = null
    var assertionResults: List<AssertionResult>? = null
}

class AssertionResult {
    var ancestorTitles: List<String>? = null
    var fullName: String? = null
    var status: String? = null
    var title: String? = null
    var duration: Int? = null
    var startTime: Long? = null
    var failureMessages: List<String>? = null

    fun isSuccess(): Boolean {
        return status != "failed"
    }

    fun getFailureMessage(): String {
        if (failureMessages?.isNotEmpty() == true) {
            return failureMessages!![0]
        }
        return "Failed"
    }
}