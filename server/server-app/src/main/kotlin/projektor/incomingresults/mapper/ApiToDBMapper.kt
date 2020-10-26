package projektor.incomingresults.mapper

import projektor.server.api.TestRunSummary
import java.time.LocalDateTime
import java.time.ZoneId
import projektor.database.generated.tables.pojos.TestRun as TestRunDB

fun TestRunSummary.toDB(): TestRunDB {
    val testRunDB = TestRunDB()
    testRunDB.publicId = id
    testRunDB.totalTestCount = totalTestCount
    testRunDB.totalPassingCount = totalPassingCount
    testRunDB.totalFailureCount = totalFailureCount
    testRunDB.totalSkippedCount = totalSkippedCount
    testRunDB.passed = passed
    testRunDB.cumulativeDuration = cumulativeDuration
    testRunDB.averageDuration = averageDuration
    testRunDB.slowestTestCaseDuration = slowestTestCaseDuration
    testRunDB.createdTimestamp = LocalDateTime.ofInstant(createdTimestamp, ZoneId.of("Z"))
    testRunDB.wallClockDuration = wallClockDuration

    return testRunDB
}
