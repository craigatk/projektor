package projektor.incomingresults

import org.apache.commons.lang3.RandomStringUtils
import projektor.server.api.PublicId

fun randomPublicId() = PublicId(RandomStringUtils.randomAlphanumeric(12).uppercase())
