package projektor.plugin.file

import spock.lang.Specification

class DirectoryUtilSpec extends Specification {
    File baseDir

    def setup() {
        baseDir
    }

    def "should find subdirectory path for one level deep"() {
        given:
        File subDir = baseDir.newFolder("sub")

        when:
        String subDirPath = DirectoryUtil.findSubDirectoryPath(baseDir.root, subDir)

        then:
        subDirPath == "sub"
    }

    def "should find subdirectory path for two levels deep"() {
        given:
        File subDir = baseDir.newFolder("level1", "level2")

        when:
        String subDirPath = DirectoryUtil.findSubDirectoryPath(baseDir.root, subDir)

        then:
        subDirPath == "level1/level2"
    }

    def "should find empty subdirectory when directories are the same"() {
        when:
        String subDirPath = DirectoryUtil.findSubDirectoryPath(baseDir.root, baseDir.root)

        then:
        subDirPath == ""
    }
}
