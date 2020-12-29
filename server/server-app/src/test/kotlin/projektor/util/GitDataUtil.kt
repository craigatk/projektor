package projektor.util

import org.apache.commons.lang3.RandomStringUtils

fun randomOrgAndRepo(): String = "${RandomStringUtils.randomAlphabetic(8)}/${RandomStringUtils.randomAlphabetic(8)}"
