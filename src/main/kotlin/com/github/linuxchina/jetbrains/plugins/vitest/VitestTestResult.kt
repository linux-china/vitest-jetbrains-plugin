package com.github.linuxchina.jetbrains.plugins.vitest


class VitestTestResult {
    var numTotalTestSuites: Int? = null
    var numPassedTestSuites: Int? = null
    var numFailedTestSuites: Int? = null
    var numPendingTestSuites: Int? = null
    var numTotalTests: Int = 1
    var numPassedTests: Int = 0
    var numFailedTests: Int = 0
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

    fun getStatistics(): String {
        val percentage = (numPassedTests * 100) / numTotalTests
        return "${numPassedTests}/${numTotalTests}(${percentage}%) tests passed"
    }
}

class TestResult {
    var startTime: Long = 0
    var endTime: Long = 0
    var status: String? = null
    var message: String? = null
    var name: String? = null
    var assertionResults: List<AssertionResult>? = null

    fun getStatistics(): String {
        var failedCount = 0
        var successCount = 0
        assertionResults?.forEach {
            if (it.isSuccess()) {
                successCount += 1
            } else {
                failedCount += 1
            }
        }
        val totalCount = failedCount + successCount
        val percentage = if (totalCount > 0) {
            (successCount * 100) / totalCount
        } else {
            100
        }
        val duration = endTime - startTime
        return "${successCount}/${totalCount}(${percentage}%) ${duration}ms"
    }
}

class AssertionResult {
    var ancestorTitles: List<String>? = null
    var fullName: String? = null
    var status: String? = null
    var title: String? = null
    var duration: Int = 0
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

    override fun toString(): String {
        return "${title} ${duration}ms"
    }

}