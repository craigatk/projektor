package projektor.coverage

import projektor.server.api.coverage.CoverageFile

data class GroupedCoverageFile(val groupName: String, val coverageFile: CoverageFile) {
    val path: String
        get() = normalizeCoverageFilePath(coverageFile.fullPath())
}

fun CoverageFile.fullPath(): String = filePath ?: if (directoryName.isNotEmpty()) "$directoryName/$fileName" else fileName

fun normalizeCoverageFilePath(path: String): String = path.trim().replace('\\', '/').removePrefix("./").trimStart('/')

fun coverageFilePathsMatch(
    coverageFilePath: String,
    requestedPath: String,
): Boolean =
    requestedPath.isNotEmpty() &&
        (
            coverageFilePath == requestedPath ||
                coverageFilePath.endsWith("/$requestedPath") ||
                requestedPath.endsWith("/$coverageFilePath")
        )
