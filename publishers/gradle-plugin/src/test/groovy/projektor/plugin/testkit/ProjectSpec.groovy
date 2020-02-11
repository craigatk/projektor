package projektor.plugin.testkit

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import projektor.plugin.WireMockStubber
import projektor.plugin.testkit.util.SpecWriter
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

abstract class ProjectSpec extends Specification {
    @Rule
    TemporaryFolder projectRootDir = new TemporaryFolder()

    @Rule
    WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort())

    WireMockStubber wireMockStubber = new WireMockStubber(wireMockRule)

    SpecWriter specWriter = new SpecWriter()

    String serverUrl

    def setup() {
        serverUrl = wireMockStubber.serverUrl
    }
}
