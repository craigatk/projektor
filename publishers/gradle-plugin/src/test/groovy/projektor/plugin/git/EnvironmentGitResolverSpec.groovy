package projektor.plugin.git

import org.gradle.api.logging.Logger
import spock.lang.Specification
import spock.lang.Unroll

class EnvironmentGitResolverSpec extends Specification {
    private EnvironmentResolver environmentResolver = Mock()
    private Logger logger = Mock()

    def "when one of the ref environment variables set should find branch name"() {
        given:
        GitResolutionConfig config = new GitResolutionConfig(
                refEnvironmentVariables: ["ref1", "ref2"]
        )
        EnvironmentGitResolver gitResolver = new EnvironmentGitResolver(config, environmentResolver, logger)

        when:
        String branchName = gitResolver.findBranchName()

        then:
        1 * environmentResolver.findFirstEnvironmentValue(["ref1", "ref2"]) >> "refs/heads/main"

        and:
        branchName == "main"
    }

    def "when no ref environment variable set should return null ref"() {
        given:
        GitResolutionConfig config = new GitResolutionConfig(
                refEnvironmentVariables: ["ref1", "ref2"]
        )
        EnvironmentGitResolver gitResolver = new EnvironmentGitResolver(config, environmentResolver, logger)

        when:
        String branchName = gitResolver.findBranchName()

        then:
        1 * environmentResolver.findFirstEnvironmentValue(["ref1", "ref2"]) >> null

        and:
        branchName == null
    }

    @Unroll
    def "when branch is #branchName should be main branch #shouldBeMainBranch"() {
        given:
        GitResolutionConfig config = new GitResolutionConfig(
                refEnvironmentVariables: ["ref1", "ref2"],
                mainBranchNames: ["main", "master"]
        )
        EnvironmentGitResolver gitResolver = new EnvironmentGitResolver(config, environmentResolver, logger)

        when:
        boolean isMainBranch = gitResolver.isMainBranch()

        then:
        1 * environmentResolver.findFirstEnvironmentValue(["ref1", "ref2"]) >> "refs/heads/$branchName"

        and:
        isMainBranch == shouldBeMainBranch

        where:
        branchName  || shouldBeMainBranch
        "main"      || true
        "feature-1" || false
    }

    def "should find repository"() {
        given:
        GitResolutionConfig config = new GitResolutionConfig(
                repoEnvironmentVariables: ["repo1", "repo2"]
        )
        EnvironmentGitResolver gitResolver = new EnvironmentGitResolver(config, environmentResolver, logger)

        String repoEnvironmentValue = "projektor/projektor-doc"

        when:
        String repositoryName = gitResolver.findRepository()

        then:
        1 * environmentResolver.findFirstEnvironmentValue(["repo1", "repo2"]) >> repoEnvironmentValue

        and:
        repositoryName == repoEnvironmentValue
    }
}
