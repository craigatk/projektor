package projektor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.matchContentType
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.MeterRegistry
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger
import projektor.attachment.AttachmentConfig
import projektor.attachment.AttachmentDatabaseRepository
import projektor.attachment.AttachmentRepository
import projektor.attachment.AttachmentService
import projektor.auth.AuthConfig
import projektor.auth.AuthService
import projektor.badge.CoverageBadgeService
import projektor.cleanup.AttachmentCleanupService
import projektor.cleanup.CleanupConfig
import projektor.cleanup.CleanupScheduledJob
import projektor.cleanup.TestRunCleanupService
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.database.DataSourceConfig
import projektor.error.ProcessingFailureService
import projektor.incomingresults.GroupedTestResultsService
import projektor.incomingresults.TestResultsProcessingService
import projektor.incomingresults.TestResultsService
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.message.MessageConfig
import projektor.message.MessageService
import projektor.metadata.TestRunMetadataService
import projektor.metrics.InfluxMetricsConfig
import projektor.metrics.MetricsService
import projektor.metrics.createRegistry
import projektor.notification.NotificationConfig
import projektor.notification.github.GitHubClientConfig
import projektor.notification.github.GitHubNotificationConfig
import projektor.notification.github.auth.JwtProvider
import projektor.notification.github.auth.JwtTokenConfig
import projektor.notification.github.comment.GitHubCommentClient
import projektor.notification.github.comment.GitHubCommentService
import projektor.organization.coverage.OrganizationCoverageService
import projektor.performance.PerformanceResultsService
import projektor.quality.CodeQualityReportRepository
import projektor.repository.coverage.RepositoryCoverageService
import projektor.repository.performance.RepositoryPerformanceService
import projektor.repository.testrun.RepositoryTestRunService
import projektor.route.attachments
import projektor.route.badge
import projektor.route.codeQuality
import projektor.route.config
import projektor.route.coverage
import projektor.route.failure
import projektor.route.health
import projektor.route.messages
import projektor.route.metadata
import projektor.route.organization
import projektor.route.performance
import projektor.route.previousRuns
import projektor.route.repository
import projektor.route.repositoryCoverage
import projektor.route.repositoryPerformance
import projektor.route.results
import projektor.route.testCases
import projektor.route.testRunSystemAttributes
import projektor.route.testRuns
import projektor.route.testSuites
import projektor.route.ui
import projektor.route.version
import projektor.schedule.Scheduler
import projektor.telemetry.OpenTelemetryRoute
import projektor.testcase.TestCaseService
import projektor.testrun.TestRunRepository
import projektor.testrun.TestRunService
import projektor.testrun.attributes.TestRunSystemAttributesService
import projektor.testsuite.TestSuiteService
import projektor.versioncontrol.VersionControlConfig

fun Application.main(meterRegistry: MeterRegistry? = null) {
    val applicationConfig = environment.config

    val authConfig = AuthConfig.createAuthConfig(applicationConfig)

    val influxMetricsConfig = InfluxMetricsConfig.createInfluxMetricsConfig(applicationConfig)
    val metricRegistry = meterRegistry ?: createRegistry(influxMetricsConfig)

    val dataSourceConfig = DataSourceConfig.createDataSourceConfig(applicationConfig)
    val dataSource = DataSourceConfig.createDataSource(dataSourceConfig, metricRegistry)
    DataSourceConfig.flywayMigrate(dataSource, dataSourceConfig)
    val dslContext = DataSourceConfig.createDSLContext(dataSource, dataSourceConfig)

    val cleanupConfig = CleanupConfig.createCleanupConfig(applicationConfig)

    val messageConfig = MessageConfig.createMessageConfig(applicationConfig)

    val versionControlConfig = VersionControlConfig.createVersionControlConfig(applicationConfig)

    val notificationConfig = NotificationConfig.createNotificationConfig(applicationConfig)
    val gitHubCommentService = conditionallyCreateGitHubCommentService(applicationConfig)

    val attachmentRepository: AttachmentRepository = AttachmentDatabaseRepository(dslContext)
    val attachmentService = conditionallyCreateAttachmentService(applicationConfig, attachmentRepository)
    attachmentService?.conditionallyCreateBucketIfNotExists()

    val appModule = createAppModule(
        dataSource = dataSource,
        authConfig = authConfig,
        dslContext = dslContext,
        metricRegistry = metricRegistry,
        messageConfig = messageConfig,
        notificationConfig = notificationConfig,
        gitHubCommentService = gitHubCommentService,
        attachmentService = attachmentService
    )

    install(CORS) {
        anyHost()
    }
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    install(Koin) {
        SLF4JLogger()
        modules(appModule)
    }
    install(CachingHeaders) {
        options { _, outgoingContent ->
            val oneDayInSeconds = 24 * 60 * 60
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Application.JavaScript -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 7 * oneDayInSeconds))
                else -> null
            }
        }
    }
    if (influxMetricsConfig.enabled) {
        install(MicrometerMetrics) {
            registry = metricRegistry

            influxMetricsConfig.environment?.let { metricsEnv ->
                timers { _, _ ->
                    this.tag("env", metricsEnv)
                }
            }
        }
    }
    install(Compression) {
        gzip {
            matchContentType(ContentType.Application.Json, ContentType.Application.JavaScript)
        }
    }
    install(IgnoreTrailingSlash) // Needed to treat /tests/ and /tests as the same URL
    install(OpenTelemetryRoute)

    val authService: AuthService by inject()
    val messageService: MessageService by inject()
    val testResultsService: TestResultsService by inject()
    val groupedTestResultsService: GroupedTestResultsService by inject()
    val previousTestRunService: PreviousTestRunService by inject()
    val testResultsProcessingService: TestResultsProcessingService by inject()
    val testRunService: TestRunService by inject()
    val testCaseService: TestCaseService by inject()
    val testSuiteService: TestSuiteService by inject()
    val testRunSystemAttributesService: TestRunSystemAttributesService by inject()
    val testRunRepository: TestRunRepository by inject()
    val resultsProcessingRepository: ResultsProcessingRepository by inject()

    val coverageService: CoverageService by inject()

    val testRunCleanupService = TestRunCleanupService(cleanupConfig, testRunRepository, resultsProcessingRepository, coverageService, attachmentService)
    val attachmentCleanupService = attachmentService?.let { AttachmentCleanupService(cleanupConfig, testRunRepository, attachmentService) }
    val scheduler: Scheduler by inject()
    CleanupScheduledJob.conditionallyStartCleanupScheduledJob(cleanupConfig, testRunCleanupService, attachmentCleanupService, scheduler)

    val testRunMetadataService: TestRunMetadataService by inject()

    val organizationCoverageService: OrganizationCoverageService by inject()

    val repositoryCoverageService: RepositoryCoverageService by inject()
    val repositoryPerformanceService: RepositoryPerformanceService by inject()
    val repositoryTestRunService: RepositoryTestRunService by inject()

    val performanceResultsService: PerformanceResultsService by inject()

    val coverageBadgeService: CoverageBadgeService by inject()

    val metricsService: MetricsService by inject()

    val processingFailureService: ProcessingFailureService by inject()

    val codeQualityReportRepository: CodeQualityReportRepository by inject()

    routing {
        attachments(attachmentService, authService)
        badge(coverageBadgeService)
        codeQuality(codeQualityReportRepository)
        config(cleanupConfig)
        coverage(authService, coverageService)
        failure(processingFailureService)
        health()
        messages(messageService)
        metadata(testRunMetadataService, versionControlConfig)
        organization(organizationCoverageService)
        performance(performanceResultsService)
        previousRuns(previousTestRunService)
        repository(repositoryTestRunService)
        repositoryCoverage(coverageService, previousTestRunService, repositoryCoverageService)
        repositoryPerformance(repositoryPerformanceService)
        results(testResultsService, groupedTestResultsService, testResultsProcessingService, authService, metricRegistry, metricsService)
        testCases(testCaseService)
        testSuites(testSuiteService)
        testRuns(testRunService)
        testRunSystemAttributes(testRunSystemAttributesService)
        ui()
        version()
    }
}

private fun conditionallyCreateAttachmentService(applicationConfig: ApplicationConfig, attachmentRepository: AttachmentRepository): AttachmentService? =
    if (AttachmentConfig.attachmentsEnabled(applicationConfig))
        AttachmentService(AttachmentConfig.createAttachmentConfig(applicationConfig), attachmentRepository)
    else
        null

private fun conditionallyCreateGitHubCommentService(applicationConfig: ApplicationConfig): GitHubCommentService? {
    val gitHubNotificationConfig = GitHubNotificationConfig.createGitHubNotificationConfig(applicationConfig)
    val (gitHubApiUrl, gitHubAppId, privateKey) = gitHubNotificationConfig

    return if (gitHubApiUrl != null && gitHubAppId != null && privateKey != null) {
        val jwtTokenConfig = JwtTokenConfig(
            gitHubAppId = gitHubAppId,
            pemContents = privateKey,
            ttlMillis = 60_000
        )
        val jwtProvider = JwtProvider(jwtTokenConfig)

        val gitHubClientConfig = GitHubClientConfig(gitHubApiUrl = gitHubApiUrl)

        val gitHubCommentClient = GitHubCommentClient(
            clientConfig = gitHubClientConfig,
            jwtProvider = jwtProvider
        )

        GitHubCommentService(gitHubCommentClient)
    } else {
        null
    }
}
