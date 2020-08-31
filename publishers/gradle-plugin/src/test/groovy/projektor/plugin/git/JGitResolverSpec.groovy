package projektor.plugin.git

import org.gradle.api.logging.Logger
import spock.lang.Specification

class JGitResolverSpec extends Specification {
    private Logger logger = Mock()

    def "should get branch name"() {
        given:
        GitResolutionConfig config = new GitResolutionConfig(enabled: true, mainBranchNames: ["main", "master"])
        JGitResolver jGitResolver = new JGitResolver(config, logger)

        when:
        String branchName = jGitResolver.findBranchName()

        then:
        branchName != null
    }

    def "should get repository name"() {
        given:
        GitResolutionConfig config = new GitResolutionConfig(enabled: true, mainBranchNames: ["main", "master"])
        JGitResolver jGitResolver = new JGitResolver(config, logger)

        when:
        String repository = jGitResolver.findRepository()

        then:
        repository == "craigatk/projektor" || repository.endsWith("projektor")
    }
}
