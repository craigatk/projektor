package projektor.testcase

import projektor.server.api.TestCase

fun buildTestCaseDebugContextMarkdown(
    testCase: TestCase,
    systemOut: String?,
    systemErr: String?,
): String {
    val failure = testCase.failure

    return buildString {
        appendLine("# Test Failure: ${testCase.name}")
        appendLine()
        appendLine("- **Full name:** ${testCase.fullName}")
        testCase.testSuiteName?.let { appendLine("- **Suite:** $it") }
        testCase.className?.let { appendLine("- **Class:** $it") }
        testCase.fileName?.let { appendLine("- **File:** $it") }
        appendLine("- **Duration:** ${testCase.duration ?: "unknown"} ms")

        if (failure != null) {
            appendLine()
            appendLine("## Failure")

            failure.failureType?.let {
                appendLine()
                appendLine("**Type:** $it")
            }

            failure.failureMessage?.let {
                appendLine()
                appendLine("**Message:**")
                appendLine("```")
                appendLine(it)
                appendLine("```")
            }

            failure.failureText?.let {
                appendLine()
                appendLine("**Stack trace:**")
                appendLine("```")
                appendLine(it)
                appendLine("```")
            }
        }

        if (!systemOut.isNullOrBlank()) {
            appendLine()
            appendLine("## System out")
            appendLine("```")
            appendLine(systemOut)
            appendLine("```")
        }

        if (!systemErr.isNullOrBlank()) {
            appendLine()
            appendLine("## System err")
            appendLine("```")
            appendLine(systemErr)
            appendLine("```")
        }
    }.trim()
}
