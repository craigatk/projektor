package projektor.incomingresults.mapper

import java.sql.Timestamp
import java.time.ZoneOffset
import projektor.database.generated.tables.pojos.TestCase as TestCaseDB
import projektor.database.generated.tables.pojos.TestFailure as TestFailureDB
import projektor.database.generated.tables.pojos.TestSuite as TestSuiteDB
import projektor.database.generated.tables.pojos.TestSuiteGroup as TestSuiteGroupDB
import projektor.incomingresults.model.GroupedTestSuites
import projektor.parser.model.Failure
import projektor.parser.model.TestCase
import projektor.parser.model.TestSuite

fun GroupedTestSuites.toDB(testRunId: Long): TestSuiteGroupDB {
    val testSuiteGroupDB = TestSuiteGroupDB()
    testSuiteGroupDB.testRunId = testRunId
    testSuiteGroupDB.groupName = groupName
    testSuiteGroupDB.groupLabel = groupLabel
    testSuiteGroupDB.directory = directory

    return testSuiteGroupDB
}

fun TestSuite.toDB(testRunId: Long, testGroupId: Long?, testSuiteIdx: Int): TestSuiteDB {
    val packageAndClassName = parsePackageAndClassName(name)

    val testSuiteDB = TestSuiteDB()
    testSuiteDB.testRunId = testRunId
    testSuiteDB.testSuiteGroupId = testGroupId
    testSuiteDB.idx = testSuiteIdx
    testSuiteDB.packageName = packageAndClassName.first
    testSuiteDB.className = packageAndClassName.second
    testSuiteDB.testCount = tests
    testSuiteDB.passingCount = passingCount
    testSuiteDB.skippedCount = skipped
    testSuiteDB.failureCount = failures + errors
    testSuiteDB.startTs = if (timestamp != null) Timestamp.from(timestamp.toInstant(ZoneOffset.UTC)) else null
    testSuiteDB.hostname = hostname
    testSuiteDB.duration = time
    testSuiteDB.systemOut = systemOut
    testSuiteDB.systemErr = systemErr
    testSuiteDB.hasSystemOut = !systemOut.isNullOrBlank()
    testSuiteDB.hasSystemErr = !systemErr.isNullOrBlank()

    return testSuiteDB
}

fun TestCase.toDB(testSuiteId: Long, testCaseIdx: Int): TestCaseDB {
    val packageAndClassName = parsePackageAndClassName(className)

    val testCaseDB = TestCaseDB()
    testCaseDB.testSuiteId = testSuiteId
    testCaseDB.idx = testCaseIdx
    testCaseDB.name = name
    testCaseDB.packageName = packageAndClassName.first
    testCaseDB.className = packageAndClassName.second
    testCaseDB.duration = time
    testCaseDB.passed = failure == null
    testCaseDB.skipped = skipped != null

    return testCaseDB
}

fun Failure.toDB(testCaseId: Long): TestFailureDB {
    val testFailureDB = TestFailureDB()
    testFailureDB.testCaseId = testCaseId
    testFailureDB.failureMessage = message
    testFailureDB.failureType = type
    testFailureDB.failureText = text

    return testFailureDB
}

fun parsePackageAndClassName(classNameWithPackage: String): Pair<String?, String> =
    if (classNameWithPackage.contains('.')) {
        val packageEndIndex = classNameWithPackage.lastIndexOf('.')

        val packageName = classNameWithPackage.substring(0, packageEndIndex)
        val className = classNameWithPackage.substring(packageEndIndex + 1)

        Pair(packageName, className)
    } else {
        Pair(null, classNameWithPackage)
    }
