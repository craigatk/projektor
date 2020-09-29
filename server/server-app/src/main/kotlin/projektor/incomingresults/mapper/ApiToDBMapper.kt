package projektor.incomingresults.mapper

import projektor.server.api.TestRunSummary
import java.sql.Timestamp
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
    testRunDB.createdTimestamp = Timestamp.from(createdTimestamp)
    testRunDB.wallClockDuration = wallClockDuration

    return testRunDB
}
