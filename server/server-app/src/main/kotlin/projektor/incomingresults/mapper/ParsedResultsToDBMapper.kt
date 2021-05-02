package projektor.incomingresults.mapper

import org.slf4j.LoggerFactory
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.model.ResultsMetadata
import projektor.parser.model.Failure
import projektor.parser.model.TestCase
import projektor.parser.model.TestSuite
import projektor.database.generated.tables.pojos.GitMetadata as GitMetadataDB
import projektor.database.generated.tables.pojos.ResultsMetadata as ResultsMetadataDB
import projektor.database.generated.tables.pojos.TestCase as TestCaseDB
import projektor.database.generated.tables.pojos.TestFailure as TestFailureDB
import projektor.database.generated.tables.pojos.TestSuite as TestSuiteDB
import projektor.database.generated.tables.pojos.TestSuiteGroup as TestSuiteGroupDB

private val logger = LoggerFactory.getLogger("ParsedResultsToDBMapper")

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
    testSuiteDB.fileName = normalizeFilePath(file)
    testSuiteDB.packageName = packageAndClassName.first
    testSuiteDB.className = packageAndClassName.second
    testSuiteDB.testCount = tests
    testSuiteDB.passingCount = passingCount
    testSuiteDB.skippedCount = skipped
    testSuiteDB.failureCount = failures + errors
    testSuiteDB.startTs = timestamp
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
    testCaseDB.systemErr = systemErr
    testCaseDB.systemOut = systemOut
    testCaseDB.hasSystemErr = systemErr != null
    testCaseDB.hasSystemOut = systemOut != null

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

fun parsePackageAndClassName(classNameWithPackage: String?): Pair<String?, String?> =
    try {
        if (classNameWithPackage != null) {
            val cleanedClassNameWithPackage = classNameWithPackage.replace("\\", "/")

            val isFilePath = cleanedClassNameWithPackage.contains("/") &&
                cleanedClassNameWithPackage.contains(".") &&
                !cleanedClassNameWithPackage.contains(" ")

            val isJvmPackage = cleanedClassNameWithPackage.contains('.') &&
                !cleanedClassNameWithPackage.contains(" ")

            if (isFilePath) {
                val lastSlashIndex = cleanedClassNameWithPackage.lastIndexOf("/")
                val fileNameEndIndex = cleanedClassNameWithPackage.indexOf(".", lastSlashIndex)

                if (fileNameEndIndex > 0) {
                    val fileName = cleanedClassNameWithPackage.substring(lastSlashIndex + 1, fileNameEndIndex)

                    // A leading forward slash messes up the URL
                    val packageStartIndex = if (cleanedClassNameWithPackage.startsWith("/")) 1 else 0

                    val packageName = cleanedClassNameWithPackage.substring(packageStartIndex)

                    Pair(packageName, fileName)
                } else {
                    Pair(null, cleanedClassNameWithPackage)
                }
            } else if (isJvmPackage) {
                val packageEndIndex = cleanedClassNameWithPackage.lastIndexOf('.')

                val packageName = cleanedClassNameWithPackage.substring(0, packageEndIndex)
                val className = cleanedClassNameWithPackage.substring(packageEndIndex + 1)

                Pair(packageName, className)
            } else {
                Pair(null, cleanedClassNameWithPackage)
            }
        } else {
            Pair(null, classNameWithPackage)
        }
    } catch (e: Exception) {
        logger.warn("Failed to parse class name with package $classNameWithPackage", e)
        Pair(null, classNameWithPackage)
    }

fun normalizeFilePath(fileName: String?) = fileName?.replace("\\", "/")

fun ResultsMetadata.toDB(testRunId: Long): ResultsMetadataDB {
    val resultsMetadataDB = ResultsMetadataDB()
    resultsMetadataDB.testRunId = testRunId
    resultsMetadataDB.ci = ci
    resultsMetadataDB.group = group

    return resultsMetadataDB
}

fun GitMetadata.toDB(testRunId: Long): GitMetadataDB {
    val gitMetadataDB = GitMetadataDB()
    gitMetadataDB.testRunId = testRunId
    gitMetadataDB.repoName = repoName
    gitMetadataDB.orgName = repoName?.split("/")?.firstOrNull()
    gitMetadataDB.projectName = projectName
    gitMetadataDB.branchName = branchName
    gitMetadataDB.isMainBranch = isMainBranch

    return gitMetadataDB
}
