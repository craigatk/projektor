package projektor.testcase.cluster

import projektor.server.api.TestCase
import java.security.MessageDigest

/**
 * A normalized fingerprint of a test failure. Failures that share a signature almost certainly
 * share a root cause, e.g. 38 tests that all fail with a ConnectException thrown from DbSetup.
 *
 * The signature is built from the root cause (the last "Caused by:" section of a stack trace),
 * its exception type, its message with volatile values (numbers, UUIDs, hashes) masked out,
 * and the first stack frame from the project's own code (line numbers ignored).
 */
data class FailureSignature(
    val failureType: String?,
    val normalizedMessage: String?,
    val location: StackLocation?,
) {
    val key: String by lazy {
        val raw = listOf(failureType.orEmpty(), normalizedMessage.orEmpty(), location?.key.orEmpty()).joinToString("|")
        MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(16)
    }

    val title: String
        get() {
            val typeName = failureType?.let(::simpleTypeName)

            return when {
                typeName != null && location != null -> "$typeName in ${location.displayOwner}"
                typeName != null && normalizedMessage != null -> "$typeName: ${normalizedMessage.truncate(TITLE_MESSAGE_LENGTH)}"
                typeName != null -> typeName
                normalizedMessage != null -> normalizedMessage.truncate(TITLE_MESSAGE_LENGTH)
                location != null -> "Failure in ${location.displayOwner}"
                else -> "Unknown failure"
            }
        }

    companion object {
        private const val TITLE_MESSAGE_LENGTH = 100
        private const val MAX_MESSAGE_LENGTH = 300

        private val causedByPrefix = Regex("""^\s*Caused by:\s*""")

        // e.g. "java.net.ConnectException: Connection refused" or "TypeError: x is undefined"
        private val exceptionHeader = Regex("""^([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)(?::\s*(.*))?$""")
        private val exceptionTypeName = Regex("""^(?:.*(?:Exception|Error|Throwable|Failure|Failed)|Error)$""")

        // e.g. "at java.base/sun.nio.ch.Net.connect0(Native Method)" or "at com.acme.DbSetup.connect(DbSetup.kt:42)"
        private val jvmFrame = Regex("""^\s*at\s+(?:[\w.$-]+(?:@[\w.-]+)?/)?([\w$.]+)\.([\w$<>-]+)\(.*\)\s*$""")

        // e.g. "at Object.<anonymous> (src/db.test.js:12:5)" or "at src/db.js:12:5"
        private val jsFrame = Regex("""^\s*at\s+(?:(.+?)\s+\()?([^()\s]+?):\d+(?::\d+)?\)?\s*$""")

        private val frameworkPackagePrefixes =
            listOf(
                "java.",
                "javax.",
                "jdk.",
                "sun.",
                "com.sun.",
                "kotlin.",
                "kotlinx.",
                "scala.",
                "groovy.",
                "org.codehaus.groovy.",
                "org.apache.groovy.",
                "org.spockframework.",
                "org.junit.",
                "junit.",
                "org.opentest4j.",
                "org.testng.",
                "org.gradle.",
                "worker.org.gradle.",
                "org.assertj.",
                "org.hamcrest.",
                "strikt.",
                "io.kotest.",
                "org.mockito.",
                "io.mockk.",
                "jdk.internal.",
                "org.apache.maven.surefire.",
            )

        private val volatileValueReplacements =
            listOf(
                Regex("""[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}""") to "<uuid>",
                Regex("""\b0x[0-9a-fA-F]+\b""") to "<hex>",
                Regex("""@[0-9a-fA-F]{4,}\b""") to "@<hex>",
                Regex("""\b(?=[0-9a-fA-F]*\d)[0-9a-fA-F]{12,}\b""") to "<hex>",
                Regex("""\d+""") to "<n>",
                Regex("""\s+""") to " ",
            )

        fun fromTestCase(testCase: TestCase): FailureSignature =
            fromFailure(
                failureType = testCase.failure?.failureType,
                failureMessage = testCase.failure?.failureMessage,
                failureText = testCase.failure?.failureText,
                projectPackage = testCase.packageName ?: testCase.className?.substringBeforeLast('.', ""),
            )

        fun fromFailure(
            failureType: String?,
            failureMessage: String?,
            failureText: String?,
            projectPackage: String? = null,
        ): FailureSignature {
            val lines = (failureText?.takeIf { it.isNotBlank() } ?: failureMessage.orEmpty()).lines()

            val rootCauseStart = lines.indexOfLast { causedByPrefix.containsMatchIn(it) }.coerceAtLeast(0)
            val rootCauseHeader =
                lines.drop(rootCauseStart)
                    .firstOrNull { it.isNotBlank() }
                    ?.replace(causedByPrefix, "")
                    ?.trim()

            val headerMatch = rootCauseHeader?.let(exceptionHeader::matchEntire)
            val headerType = headerMatch?.groupValues?.get(1)?.takeIf { exceptionTypeName.matches(simpleTypeName(it)) }

            val (type, message) =
                if (headerType != null) {
                    headerType to headerMatch.groupValues[2].ifBlank { null }
                } else if (rootCauseStart > 0) {
                    // A "Caused by:" line we couldn't parse as "Type: message"; use it verbatim
                    null to rootCauseHeader
                } else {
                    failureType?.trim()?.ifBlank { null } to (failureMessage?.lines()?.firstOrNull { it.isNotBlank() } ?: rootCauseHeader)
                }

            // The root cause frequently has only JDK/driver frames (e.g. "... 42 more"),
            // so fall back to the enclosing traces to find where in the project it came from.
            val frames = (lines.drop(rootCauseStart) + lines.take(rootCauseStart)).mapNotNull(::parseFrame)
            val nonFrameworkFrames = frames.filterNot { it.isFramework() }
            val projectPrefix = projectPackage?.split('.')?.take(2)?.takeIf { it.size == 2 }?.joinToString(".", postfix = ".")
            val location =
                projectPrefix?.let { prefix -> nonFrameworkFrames.firstOrNull { it.owner.startsWith(prefix) } }
                    ?: nonFrameworkFrames.firstOrNull()

            return FailureSignature(
                failureType = type,
                normalizedMessage = message?.let(::normalizeMessage)?.ifBlank { null },
                location = location,
            )
        }

        fun normalizeMessage(message: String): String =
            volatileValueReplacements
                .fold(message) { acc, (regex, replacement) -> acc.replace(regex, replacement) }
                .trim()
                .truncate(MAX_MESSAGE_LENGTH)

        private fun parseFrame(line: String): StackLocation? {
            jvmFrame.matchEntire(line)?.let { match ->
                val (className, methodName) = match.destructured
                return StackLocation(owner = className, member = methodName, isJs = false)
            }
            jsFrame.matchEntire(line)?.let { match ->
                val (functionName, file) = match.destructured
                return StackLocation(owner = file, member = functionName.ifBlank { null }, isJs = true)
            }
            return null
        }

        private fun simpleTypeName(type: String) = type.substringAfterLast('.')

        private fun String.truncate(maxLength: Int) = if (length > maxLength) take(maxLength - 3) + "..." else this

        private fun StackLocation.isFramework(): Boolean =
            if (isJs) {
                owner.contains("node_modules") || owner.startsWith("node:") || owner.startsWith("internal/")
            } else {
                frameworkPackagePrefixes.any { owner.startsWith(it) } || owner.contains("\$\$Lambda")
            }
    }
}

/**
 * A stack frame with the line number dropped, so the same call site matches across tests.
 * For JVM frames [owner] is the fully-qualified class; for JS frames it's the file path.
 */
data class StackLocation(
    val owner: String,
    val member: String?,
    val isJs: Boolean,
) {
    val key: String
        get() = "$owner#${member.orEmpty()}"

    val displayOwner: String
        get() =
            if (isJs) {
                owner.substringAfterLast('/')
            } else {
                owner.substringAfterLast('.').substringBefore('$')
            }

    val display: String
        get() =
            when {
                member == null -> displayOwner
                isJs -> "$member ($displayOwner)"
                else -> "$displayOwner.$member"
            }
}
