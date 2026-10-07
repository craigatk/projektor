package projektor.testcase.cluster

import org.jooq.DSLContext
import projektor.database.generated.Tables.TEST_CASE
import projektor.database.generated.Tables.TEST_FAILURE
import projektor.database.generated.Tables.TEST_SUITE
import projektor.database.generated.tables.pojos.TestRun

const val CONNECTION_REFUSED_FAILURE_TEXT = """java.lang.IllegalStateException: Could not reset database
	at projektor.testing.DbSetup.reset(DbSetup.kt:18)
Caused by: java.net.ConnectException: Connection refused
	at java.base/sun.nio.ch.Net.pollConnect(Native Method)
	... 12 more"""

fun setFailureText(
    dslContext: DSLContext,
    testRun: TestRun,
    testCaseNames: List<String>,
    failureText: String,
) {
    dslContext.update(TEST_FAILURE)
        .set(TEST_FAILURE.FAILURE_TEXT, failureText)
        .set(TEST_FAILURE.FAILURE_MESSAGE, failureText.lines().first())
        .where(
            TEST_FAILURE.TEST_CASE_ID.`in`(
                dslContext.select(TEST_CASE.ID)
                    .from(TEST_CASE)
                    .join(TEST_SUITE).on(TEST_CASE.TEST_SUITE_ID.eq(TEST_SUITE.ID))
                    .where(TEST_SUITE.TEST_RUN_ID.eq(testRun.id).and(TEST_CASE.NAME.`in`(testCaseNames))),
            ),
        )
        .execute()
}
