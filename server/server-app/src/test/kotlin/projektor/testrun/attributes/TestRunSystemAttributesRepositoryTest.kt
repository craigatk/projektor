package projektor.testrun.attributes

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.incomingresults.randomPublicId
import projektor.persistence.DatabaseTestBackend
import projektor.persistence.ObjectStoreTestBackend
import projektor.persistence.RepositoryTestCase
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue

abstract class TestRunSystemAttributesRepositoryTest : RepositoryTestCase() {
    @Test
    fun `should fetch pinned when it is true`() {
        val publicId = randomPublicId()

        testRunGenerator.createTestRun(publicId, listOf())

        runBlocking { testRunSystemAttributesRepository.pin(publicId) }

        val attributes = runBlocking { testRunSystemAttributesRepository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isTrue() }
    }

    @Test
    fun `should fetch pinned when it is false`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())
        runBlocking { testRunSystemAttributesRepository.unpin(publicId) }

        val attributes = runBlocking { testRunSystemAttributesRepository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isFalse() }
    }

    @Test
    fun `should return null if attributes does not exist`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        val attributes = runBlocking { testRunSystemAttributesRepository.fetchAttributes(publicId) }

        expectThat(attributes).isNull()
    }

    @Test
    fun `should pin when no existing attributes record exists`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        runBlocking { testRunSystemAttributesRepository.pin(publicId) }

        val attributes = runBlocking { testRunSystemAttributesRepository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isTrue() }
    }

    @Test
    fun `should pin when existing pinned set to false`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())
        runBlocking { testRunSystemAttributesRepository.unpin(publicId) }

        runBlocking { testRunSystemAttributesRepository.pin(publicId) }

        val attributes = runBlocking { testRunSystemAttributesRepository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isTrue() }
    }

    @Test
    fun `should unpin when no existing attributes record exists`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())

        runBlocking { testRunSystemAttributesRepository.unpin(publicId) }

        val attributes = runBlocking { testRunSystemAttributesRepository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isFalse() }
    }

    @Test
    fun `should unpin when existing pinned set to true`() {
        val publicId = randomPublicId()
        testRunGenerator.createTestRun(publicId, listOf())
        runBlocking { testRunSystemAttributesRepository.pin(publicId) }

        runBlocking { testRunSystemAttributesRepository.unpin(publicId) }

        val attributes = runBlocking { testRunSystemAttributesRepository.fetchAttributes(publicId) }
        expectThat(attributes)
            .isNotNull()
            .and { get { pinned }.isFalse() }
    }
}

class TestRunSystemAttributesRepositoryDatabaseTest : TestRunSystemAttributesRepositoryTest() {
    override fun createBackend() = DatabaseTestBackend()
}

class TestRunSystemAttributesRepositoryObjectStoreTest : TestRunSystemAttributesRepositoryTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
