package projektor.plugin

import org.gradle.internal.impldep.org.apache.commons.lang.RandomStringUtils

import java.nio.file.Files
import java.nio.file.Path

class TempDirectory {

    Path rootPath

    TempDirectory() {
        String prefix = "gradle-spec-temp-${RandomStringUtils.randomAlphabetic(16)}"
        rootPath = Files.createTempDirectory(prefix)
    }

    File getRoot() {
        rootPath.toFile()
    }

    File newDirectory(String path) {
        File subDirectory = new File(root, path)
        subDirectory.mkdirs()
        return subDirectory
    }

    File newFile(String fileName) {
        return new File(root, fileName)
    }
}
