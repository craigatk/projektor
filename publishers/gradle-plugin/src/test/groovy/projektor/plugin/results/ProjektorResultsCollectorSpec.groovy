package projektor.plugin.results

import org.gradle.api.logging.Logger
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ProjektorResultsCollectorSpec extends Specification {
    @Rule TemporaryFolder tempFolder1
    @Rule TemporaryFolder tempFolder2

    Logger logger = Mock()
    ProjektorResultsCollector resultsCollector = new ProjektorResultsCollector(logger)

    void "should find only XML files from a single directory"() {
        given:
        String resultsXmlFile1Content = """file1line1
file1line2
file1line3"""
        File resultsXmlFile1 = tempFolder1.newFile("results1.xml")
        resultsXmlFile1.text = resultsXmlFile1Content

        String resultsXmlFile2Content = """file2line1
file2line2"""
        File resultsXmlFile2 = tempFolder1.newFile("results2.xml")
        resultsXmlFile2.text = resultsXmlFile2Content

        String otherFileContent = """Some other
text lines
In another file"""
        File otherFile = tempFolder1.newFile("results.txt")
        otherFile.text = otherFileContent

        when:
        String resultsBlob = resultsCollector.createResultsBlobFromJunitXmlResultsInDirectory(tempFolder1.root)

        then:
        resultsBlob.contains(resultsXmlFile1Content)
        resultsBlob.contains(resultsXmlFile2Content)
        !resultsBlob.contains(otherFileContent)
    }
}
