package projektor.persistence

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.incomingresults.randomPublicId
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.math.BigDecimal
import java.time.Instant

/**
 * Repository, project, organization and group names can contain any characters. The object store encodes them into
 * key segments, so names that differ only in characters with special meaning in keys or URLs must stay apart.
 */
abstract class SpecialCharacterNamesRepositoryTest : RepositoryTestCase() {
    private val uniqueOrgName = "org ${RandomStringUtils.randomAlphabetic(8).lowercase()} ü+%"

    // Names that an encoding mistake (spaces as '+', double decoding, unencoded slashes) would mix up
    private val repoNames =
        listOf(
            "$uniqueOrgName/my repo",
            "$uniqueOrgName/my+repo",
            "$uniqueOrgName/my%20repo",
            "$uniqueOrgName/my.repo/sub",
            "$uniqueOrgName/ünïcödé-repo",
        )

    private val projectNames = listOf(null, "project/with slash", "project+plus")

    @Test
    fun `repository timelines should keep runs of similarly named repositories and projects apart`() {
        val publicIdsByRepoAndProject =
            repoNames.flatMap { repoName ->
                projectNames.map { projectName ->
                    val publicId = randomPublicId()
                    testRunGenerator.createTestRunInRepo(
                        publicId,
                        listOf(TestRunGenerator.simpleTestSuite()),
                        repoName,
                        ci = true,
                        projectName = projectName,
                    )
                    Triple(repoName, projectName, publicId)
                }
            }

        publicIdsByRepoAndProject.forEach { (repoName, projectName, publicId) ->
            val timeline = runBlocking { repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoName, projectName) }

            expectThat(timeline?.timelineEntries?.map { it.publicId }).isNotNull().containsExactly(publicId.id)
        }
    }

    @Test
    fun `organization coverage should list repositories with special characters in their names`() {
        repoNames.forEach { repoName ->
            testRunGenerator.createTestRunWithCoverageAndGitMetadata(randomPublicId(), JacocoXmlLoader().serverApp(), repoName)
        }

        val reposWithCoverage = runBlocking { organizationCoverageRepository.findReposWithCoverage(uniqueOrgName) }

        expectThat(reposWithCoverage.map { it.repoName }.toSet()).isEqualTo(repoNames.toSet())
    }

    @Test
    fun `per-repository settings should keep similarly named repositories and projects apart`() {
        val names = repoNames.flatMap { repoName -> projectNames.map { projectName -> repoName to projectName } }

        runBlocking {
            names.forEachIndexed { idx, (repoName, projectName) ->
                repositorySlackChannelRepository.upsertChannelName(repoName, projectName, "#channel-$idx")
                repositoryCoverageRepository.saveLastKnownCoverageIfNewer(
                    repoName = repoName,
                    projectName = projectName,
                    branchName = "main",
                    coveredPercentage = BigDecimal(idx),
                    testRunPublicId = randomPublicId(),
                    createdTimestamp = Instant.now(),
                )
            }
        }

        names.forEachIndexed { idx, (repoName, projectName) ->
            expectThat(runBlocking { repositorySlackChannelRepository.fetchChannelName(repoName, projectName) }).isEqualTo("#channel-$idx")
            expectThat(runBlocking { repositoryCoverageRepository.fetchLastKnownCoverage(repoName, projectName) })
                .isNotNull()
                .get { coveredPercentage?.toInt() }
                .isEqualTo(idx)
        }
    }

    @Test
    fun `results group claims should keep similarly named groups apart`() {
        val repoName = repoNames.first()
        val groups = listOf("build 1", "build+1", "build%201", "build/1")

        val ownersByGroup =
            groups.associateWith { group ->
                randomPublicId().also {
                    runBlocking { testRunRepository.claimTestRunGroup(group, repoName, it) }
                }
            }

        ownersByGroup.forEach { (group, owner) ->
            expectThat(runBlocking { testRunRepository.claimTestRunGroup(group, repoName, randomPublicId()) }).isEqualTo(owner)
        }
    }
}

class SpecialCharacterNamesDatabaseTest : SpecialCharacterNamesRepositoryTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class SpecialCharacterNamesObjectStoreTest : SpecialCharacterNamesRepositoryTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
