package projektor.testrun.attributes

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.DatabaseRepositoryTestCase
import projektor.database.generated.tables.pojos.TestRunSystemAttributes
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue

class TestRunSystemAttributesDatabaseRepositoryTest : DatabaseRepositoryTestCase() {

    @Test
    fun `should fetch pinned when it is true`() {
        val repository = TestRunSystemAttributesDatabaseRepository(dslContext)

        val publicId = randomPublicId()

        testRunDBGenerator.createTestRun(publicId, listOf())

        testRunSystemAttributesDao.insert(TestRunSystemAttributes(publicId.id, true))

        val attributes = runBlocking { repository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isTrue() }
    }

    @Test
    fun `should fetch pinned when it is false`() {
        val repository = TestRunSystemAttributesDatabaseRepository(dslContext)
        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())
        testRunSystemAttributesDao.insert(TestRunSystemAttributes(publicId.id, false))

        val attributes = runBlocking { repository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isFalse() }
    }

    @Test
    fun `should return null if attributes does not exist`() {
        val repository = TestRunSystemAttributesDatabaseRepository(dslContext)
        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        val attributes = runBlocking { repository.fetchAttributes(publicId) }

        expectThat(attributes).isNull()
    }

    @Test
    fun `should pin when no existing attributes record exists`() {
        val repository = TestRunSystemAttributesDatabaseRepository(dslContext)
        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        runBlocking { repository.pin(publicId) }

        val attributes = runBlocking { repository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isTrue() }
    }

    @Test
    fun `should pin when existing pinned set to false`() {
        val repository = TestRunSystemAttributesDatabaseRepository(dslContext)
        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())
        testRunSystemAttributesDao.insert(TestRunSystemAttributes(publicId.id, false))

        runBlocking { repository.pin(publicId) }

        val attributes = runBlocking { repository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isTrue() }
    }

    @Test
    fun `should unpin when no existing attributes record exists`() {
        val repository = TestRunSystemAttributesDatabaseRepository(dslContext)
        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())

        runBlocking { repository.unpin(publicId) }

        val attributes = runBlocking { repository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isFalse() }
    }

    @Test
    fun `should unpin when existing pinned set to true`() {
        val repository = TestRunSystemAttributesDatabaseRepository(dslContext)
        val publicId = randomPublicId()
        testRunDBGenerator.createTestRun(publicId, listOf())
        testRunSystemAttributesDao.insert(TestRunSystemAttributes(publicId.id, true))

        runBlocking { repository.unpin(publicId) }

        val attributes = runBlocking { repository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isFalse() }
    }
}
