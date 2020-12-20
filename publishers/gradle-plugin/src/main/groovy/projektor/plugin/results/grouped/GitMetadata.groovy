package projektor.plugin.results.grouped

import groovy.transform.ToString

@ToString
class GitMetadata {
    String repoName
    String branchName
    boolean isMainBranch
    String commitSha
}
