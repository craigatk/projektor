package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import projektor.plugin.ResultsWireMockStubber
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

abstract class ProjectSpec extends Specification {
    @Rule
    TemporaryFolder projectRootDir = new TemporaryFolder()

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort())

    ResultsWireMockStubber resultsStubber = new ResultsWireMockStubber(wireMockRule)

    String serverUrl

    def setup() {
        serverUrl = resultsStubber.serverUrl
    }
}
