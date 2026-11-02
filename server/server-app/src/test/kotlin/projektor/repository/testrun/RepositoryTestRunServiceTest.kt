package projektor.repository.testrun

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.server.api.repository.BranchType
import strikt.api.expectThat
import strikt.assertions.containsExactly
import java.lang.reflect.Proxy

class RepositoryTestRunServiceTest {
    @Test
    fun `should cap how many test runs a flaky tests request looks at`() {
        val requestedMaxRuns = mutableListOf<Int>()

        val repository =
            Proxy.newProxyInstance(
                RepositoryTestRunRepository::class.java.classLoader,
                arrayOf(RepositoryTestRunRepository::class.java),
            ) { _, method, args ->
                when (method.name) {
                    "fetchRepositoryFailingTestCases" -> {
                        requestedMaxRuns.add(args[2] as Int)
                        listOf<Any>()
                    }
                    "fetchTestRunCount" -> 0L
                    else -> throw UnsupportedOperationException(method.name)
                }
            } as RepositoryTestRunRepository

        val service = RepositoryTestRunService(repository)

        listOf(1_000_000, 10, -5).forEach { maxRuns ->
            runBlocking { service.fetchFlakyTests("org/repo", null, maxRuns, 5, BranchType.ALL) }
        }

        expectThat(requestedMaxRuns).containsExactly(RepositoryTestRunService.MAX_FLAKY_TEST_RUNS, 10, 1)
    }
}
