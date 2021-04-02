package projektor.plugin.results

import org.gradle.api.logging.Logger
import projektor.plugin.TempDirectory
import spock.lang.Specification

class ProjektorResultsCollectorSpec extends Specification {
    TempDirectory tempFolder = new TempDirectory()

    Logger logger = Mock()
    ProjektorResultsCollector resultsCollector = new ProjektorResultsCollector(logger)

    void "should find only XML files from a single directory"() {
        given:
        String resultsXmlFile1Content = """file1line1
file1line2
file1line3"""
        File resultsXmlFile1 = tempFolder.newFile("results1.xml")
        resultsXmlFile1.text = resultsXmlFile1Content

        String resultsXmlFile2Content = """file2line1
file2line2"""
        File resultsXmlFile2 = tempFolder.newFile("results2.xml")
        resultsXmlFile2.text = resultsXmlFile2Content

        String otherFileContent = """Some other
text lines
In another file"""
        File otherFile = tempFolder.newFile("results.txt")
        otherFile.text = otherFileContent

        when:
        String resultsBlob = resultsCollector.createResultsBlobFromJunitXmlResultsInDirectory(tempFolder.root)

        then:
        resultsBlob.contains(resultsXmlFile1Content)
        resultsBlob.contains(resultsXmlFile2Content)
        !resultsBlob.contains(otherFileContent)
    }
}
