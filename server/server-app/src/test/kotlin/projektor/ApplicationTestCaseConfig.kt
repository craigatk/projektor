package projektor

import java.math.BigDecimal

data class ApplicationTestCaseConfig(
    val databaseSchema: String = "public",
    val publishToken: String? = null,
    val attachmentsEnabled: Boolean = false,
    val attachmentsMaxSizeMB: BigDecimal? = null,
    val attachmentsAccessKey: String = "minio_access_key",
    val attachmentsBucketName: String = "attachmentstesting",
    val attachmentsAutoCreateBucket: Boolean = true,
    val attachmentCleanupMaxAgeDays: Int? = null,
    val reportCleanupMaxAgeDays: Int? = null,
    val metricsEnabled: Boolean = false,
    val metricsPort: Int = 0,
    val metricsUsername: String? = null,
    val metricsPassword: String? = null,
    val globalMessages: String? = null,
    val gitHubBaseUrl: String? = null,
    val serverBaseUrl: String? = null,
    val gitHubApiUrl: String? = null,
    val gitHubAppId: String? = null,
    val gitHubPrivateKeyEncoded: String? = null,
)
