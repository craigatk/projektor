package projektor.testcase.cluster

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNull

class FailureSignatureTest {
    private fun dbConnectionFailure(
        testClassName: String,
        port: Int,
        connectionId: String,
    ) = """
        org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection for $connectionId
        	at org.springframework.jdbc.datasource.DataSourceUtils.getConnection(DataSourceUtils.java:84)
        	at com.acme.orders.testing.DbSetup.connect(DbSetup.kt:31)
        	at com.acme.orders.testing.DbSetup.reset(DbSetup.kt:18)
        	at com.acme.orders.$testClassName.setup($testClassName.kt:22)
        	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
        	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:728)
        Caused by: java.net.ConnectException: Connection refused: localhost/127.0.0.1:$port
        	at java.base/sun.nio.ch.Net.pollConnect(Native Method)
        	at java.base/sun.nio.ch.NioSocketImpl.timedFinishConnect(NioSocketImpl.java:542)
        	at org.postgresql.core.PGStream.createSocket(PGStream.java:243)
        	... 42 more
        """.trimIndent()

    @Test
    fun `should use the root cause and the first project frame`() {
        val signature =
            FailureSignature.fromFailure(
                failureType = "org.springframework.jdbc.CannotGetJdbcConnectionException",
                failureMessage = "Failed to obtain JDBC Connection",
                failureText = dbConnectionFailure("OrderRepositoryTest", 5432, "a3f9c2d4-1b2c-4d5e-8f90-123456789abc"),
                projectPackage = "com.acme.orders",
            )

        expectThat(signature.failureType).isEqualTo("java.net.ConnectException")
        expectThat(signature.normalizedMessage).isEqualTo("Connection refused: localhost/<n>.<n>.<n>.<n>:<n>")
        expectThat(signature.location?.display).isEqualTo("DbSetup.connect")
        expectThat(signature.title).isEqualTo("ConnectException in DbSetup")
    }

    @Test
    fun `failures with the same root cause in different tests should share a key`() {
        val first =
            FailureSignature.fromFailure(
                "org.springframework.jdbc.CannotGetJdbcConnectionException",
                null,
                dbConnectionFailure("OrderRepositoryTest", 5432, "a3f9c2d4-1b2c-4d5e-8f90-123456789abc"),
                "com.acme.orders",
            )
        val second =
            FailureSignature.fromFailure(
                "org.springframework.jdbc.CannotGetJdbcConnectionException",
                null,
                dbConnectionFailure("InvoiceServiceTest", 5433, "ffffffff-1b2c-4d5e-8f90-000000000000"),
                "com.acme.orders.invoice",
            )

        expectThat(first.key).isEqualTo(second.key)
    }

    @Test
    fun `assertion failures in different tests should not share a key`() {
        fun assertionFailure(testMethod: String) =
            """
            org.opentest4j.AssertionFailedError: expected: <200> but was: <500>
            	at org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)
            	at org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:150)
            	at com.acme.orders.OrderApiTest.$testMethod(OrderApiTest.kt:40)
            """.trimIndent()

        val first =
            FailureSignature.fromFailure(
                "org.opentest4j.AssertionFailedError",
                null,
                assertionFailure("createOrder"),
                "com.acme.orders",
            )
        val second =
            FailureSignature.fromFailure(
                "org.opentest4j.AssertionFailedError",
                null,
                assertionFailure("deleteOrder"),
                "com.acme.orders",
            )

        expectThat(first.normalizedMessage).isEqualTo("expected: <<n>> but was: <<n>>")
        expectThat(first.key).isNotEqualTo(second.key)
    }

    @Test
    fun `should parse JavaScript stack traces and skip node_modules frames`() {
        val failureText =
            """
            TypeError: Cannot read properties of undefined (reading 'id')
                at getUserId (/home/ci/app/src/user/session.ts:14:22)
                at Object.<anonymous> (/home/ci/app/src/user/session.test.ts:8:5)
                at Promise.then.completed (/home/ci/app/node_modules/jest-circus/build/utils.js:298:28)
            """.trimIndent()

        val signature = FailureSignature.fromFailure(null, null, failureText)

        expectThat(signature.failureType).isEqualTo("TypeError")
        expectThat(signature.location?.display).isEqualTo("getUserId (session.ts)")
        expectThat(signature.title).isEqualTo("TypeError in session.ts")
    }

    @Test
    fun `should fall back to the failure message when there is no stack trace`() {
        val signature = FailureSignature.fromFailure(null, "Timed out after 30000ms waiting for element #submit", null)

        expectThat(signature.failureType).isNull()
        expectThat(signature.location).isNull()
        expectThat(signature.normalizedMessage).isEqualTo("Timed out after <n>ms waiting for element #submit")
        expectThat(signature.title).isEqualTo("Timed out after <n>ms waiting for element #submit")
    }

    @Test
    fun `should mask volatile values in messages`() {
        expectThat(
            FailureSignature.normalizeMessage(
                "Object com.acme.Order@1b6d3586 at 0x7ffe12 id 9f86d081884c7d659a2feaa0c55ad015\n  retried 3 times",
            ),
        ).isEqualTo("Object com.acme.Order@<hex> at <hex> id <hex> retried <n> times")
    }
}
