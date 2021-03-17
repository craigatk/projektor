package projektor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.config.ApplicationConfig
import io.ktor.features.*
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.jackson.jackson
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.path
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.micrometer.core.instrument.MeterRegistry
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.SLF4JLogger
import org.slf4j.event.Level
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
import projektor.repository.coverage.RepositoryCoverageService
import projektor.repository.performance.RepositoryPerformanceService
import projektor.repository.testrun.RepositoryTestRunService
import projektor.route.*
import projektor.schedule.Scheduler
import projektor.testcase.TestCaseService
import projektor.testrun.TestRunRepository
import projektor.testrun.TestRunService
import projektor.testrun.attributes.TestRunSystemAttributesService
import projektor.testsuite.TestSuiteService
import projektor.versioncontrol.VersionControlConfig

@KtorExperimentalAPI
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
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    install(CallLogging) {
        level = Level.INFO

        filter { call -> !call.request.path().startsWith("/health") }
    }
    install(Koin) {
        SLF4JLogger()
        modules(appModule)
    }
    install(CachingHeaders) {
        options { outgoingContent ->
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
            minimumSize(1024)
        }
    }

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

    routing {
        attachments(attachmentService, authService)
        badge(coverageBadgeService)
        config(cleanupConfig)
        coverage(authService, coverageService)
        failure(processingFailureService)
        health()
        messages(messageService)
        metadata(testRunMetadataService, versionControlConfig)
        organization(organizationCoverageService)
        performance(performanceResultsService)
        previousRuns(previousTestRunService)
        repository(repositoryCoverageService, repositoryTestRunService)
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

@KtorExperimentalAPI
private fun conditionallyCreateAttachmentService(applicationConfig: ApplicationConfig, attachmentRepository: AttachmentRepository): AttachmentService? =
    if (AttachmentConfig.attachmentsEnabled(applicationConfig))
        AttachmentService(AttachmentConfig.createAttachmentConfig(applicationConfig), attachmentRepository)
    else
        null

@KtorExperimentalAPI
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
