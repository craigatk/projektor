package projektor.database

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class DataSourceConfigTest {
    @Test
    fun `should leave an already-jdbc URL untouched`() {
        val (jdbcUrl, username, password) =
            DataSourceConfig.normalizeConnectionInfo(
                "jdbc:postgresql://localhost:5432/projektordb",
                "someuser",
                "somepass",
            )

        expectThat(jdbcUrl).isEqualTo("jdbc:postgresql://localhost:5432/projektordb")
        expectThat(username).isEqualTo("someuser")
        expectThat(password).isEqualTo("somepass")
    }

    @Test
    fun `should convert a DigitalOcean-style postgres URI with embedded credentials`() {
        val (jdbcUrl, username, password) =
            DataSourceConfig.normalizeConnectionInfo(
                "postgresql://doadmin:some-password@db-postgresql-nyc1-12345.b.db.ondigitalocean.com:25060/defaultdb?sslmode=require",
                "unused",
                "unused",
            )

        expectThat(jdbcUrl).isEqualTo(
            "jdbc:postgresql://db-postgresql-nyc1-12345.b.db.ondigitalocean.com:25060/defaultdb?sslmode=require",
        )
        expectThat(username).isEqualTo("doadmin")
        expectThat(password).isEqualTo("some-password")
    }

    @Test
    fun `should convert a postgres URI without embedded credentials, falling back to configured username and password`() {
        val (jdbcUrl, username, password) =
            DataSourceConfig.normalizeConnectionInfo(
                "postgres://db-postgresql-nyc1-12345.b.db.ondigitalocean.com:25060/defaultdb?sslmode=require",
                "fallback-user",
                "fallback-pass",
            )

        expectThat(jdbcUrl).isEqualTo(
            "jdbc:postgresql://db-postgresql-nyc1-12345.b.db.ondigitalocean.com:25060/defaultdb?sslmode=require",
        )
        expectThat(username).isEqualTo("fallback-user")
        expectThat(password).isEqualTo("fallback-pass")
    }

    @Test
    fun `should URL-decode special characters in embedded credentials`() {
        val (_, username, password) =
            DataSourceConfig.normalizeConnectionInfo(
                "postgres://user%40name:p%40ss%3Aword@localhost:5432/projektordb",
                "unused",
                "unused",
            )

        expectThat(username).isEqualTo("user@name")
        expectThat(password).isEqualTo("p@ss:word")
    }
}
