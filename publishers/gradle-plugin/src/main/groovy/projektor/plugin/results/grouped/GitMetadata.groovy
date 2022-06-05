package projektor.plugin.results.grouped

import groovy.transform.ToString

@ToString
class GitMetadata {
    String repoName
    String branchName
    String projectName
    boolean isMainBranch
    String commitSha
    Integer pullRequestNumber
}
