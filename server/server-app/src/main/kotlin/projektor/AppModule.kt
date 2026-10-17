package projektor

import io.micrometer.core.instrument.MeterRegistry
import org.koin.dsl.module
import projektor.ai.analysis.AITestFailureAnalyzer
import projektor.attachment.AttachmentService
import projektor.auth.AuthConfig
import projektor.auth.AuthService
import projektor.badge.CoverageBadgeService
import projektor.badge.TestRunBadgeService
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.error.ProcessingFailureService
import projektor.incomingresults.AppendTestResultsService
import projektor.incomingresults.GroupedResultsConverter
import projektor.incomingresults.GroupedTestResultsService
import projektor.incomingresults.TestResultsProcessingService
import projektor.incomingresults.TestResultsService
import projektor.message.MessageConfig
import projektor.message.MessageService
import projektor.metadata.TestRunMetadataService
import projektor.metrics.MetricsService
import projektor.notification.NotificationConfig
import projektor.notification.badge.SvgCoverageBadgeCreator
import projektor.notification.badge.SvgTestRunBadgeCreator
import projektor.notification.github.GitHubPullRequestCommentService
import projektor.notification.github.comment.GitHubCommentService
import projektor.notification.slack.RepositorySlackChannelService
import projektor.notification.slack.SlackMainlineNotificationService
import projektor.notification.slack.SlackNotificationConfig
import projektor.notification.slack.SlackNotificationService
import projektor.organization.coverage.OrganizationCoverageService
import projektor.parser.grouped.GroupedResultsParser
import projektor.parser.performance.PerformanceResultsParser
import projektor.performance.PerformanceResultsService
import projektor.processing.ProcessingConfig
import projektor.pullrequest.PullRequestCoverageService
import projektor.pullrequest.PullRequestFailureContextService
import projektor.repository.coverage.RepositoryCoverageService
import projektor.repository.performance.RepositoryPerformanceService
import projektor.repository.testrun.RepositoryTestRunService
import projektor.results.processor.TestResultsProcessor
import projektor.schedule.Scheduler
import projektor.testcase.TestCaseService
import projektor.testrun.TestRunService
import projektor.testrun.attributes.TestRunSystemAttributesService
import projektor.testsuite.TestSuiteService

fun createAppModule(
    authConfig: AuthConfig,
    metricRegistry: MeterRegistry,
    messageConfig: MessageConfig,
    notificationConfig: NotificationConfig,
    processingConfig: ProcessingConfig,
    gitHubCommentService: GitHubCommentService?,
    attachmentService: AttachmentService?,
    aiTestFailureAnalyzer: AITestFailureAnalyzer?,
    slackNotificationConfig: SlackNotificationConfig,
    slackNotificationService: SlackNotificationService?,
) = module {
    single { TestResultsProcessor() }
    single { metricRegistry }
    single { MetricsService(metricRegistry) }
    single { AuthService(authConfig) }

    single { PerformanceResultsService(get()) }

    single { AppendTestResultsService(get(), get()) }
    single { CoverageService(get(), get(), get(), get()) }
    single { GroupedResultsParser(processingConfig.maxPayloadSize) }
    single { PerformanceResultsParser() }
    single { GroupedResultsConverter(get(), get(), get()) }
    single { GroupedTestResultsService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { MessageService(messageConfig) }
    single { PreviousTestRunService(get()) }
    single { ProcessingFailureService(get()) }
    single { TestCaseService(get(), attachmentService, aiTestFailureAnalyzer) }
    single { PullRequestFailureContextService(get(), get()) }
    single { PullRequestCoverageService(get(), get()) }
    single { TestSuiteService(get()) }
    single { TestResultsProcessingService(get()) }
    single { TestResultsService(get(), get(), get(), get()) }
    single { TestRunService(get()) }
    single { TestRunSystemAttributesService(get()) }
    single { TestRunMetadataService(get()) }

    single { Scheduler(get()) }

    single { OrganizationCoverageService(get(), get()) }

    single { RepositoryCoverageService(get(), get(), get()) }
    single { RepositoryPerformanceService(get()) }
    single { RepositoryTestRunService(get()) }

    single { GitHubPullRequestCommentService(notificationConfig, gitHubCommentService) }

    single { RepositorySlackChannelService(get(), slackNotificationService) }
    single {
        SlackMainlineNotificationService(notificationConfig, slackNotificationConfig, slackNotificationService, get(), get(), get())
    }

    single { CoverageBadgeService(get(), get(), get(), get()) }
    single { SvgCoverageBadgeCreator() }
    single { TestRunBadgeService(get(), get(), get()) }
    single { SvgTestRunBadgeCreator() }
}
