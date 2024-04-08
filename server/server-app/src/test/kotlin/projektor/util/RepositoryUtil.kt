package projektor.util

import org.apache.commons.lang3.RandomStringUtils

fun randomFullRepoName(): String {
    val orgName = RandomStringUtils.randomAlphabetic(12)

    return "$orgName/repo"
}
