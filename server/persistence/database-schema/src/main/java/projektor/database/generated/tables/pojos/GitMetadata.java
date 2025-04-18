/*
 * This file is generated by jOOQ.
 */
package projektor.database.generated.tables.pojos;


import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class GitMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long testRunId;
    private String repoName;
    private Boolean isMainBranch;
    private String branchName;
    private String orgName;
    private String projectName;
    private Integer pullRequestNumber;
    private String commitSha;

    public GitMetadata() {}

    public GitMetadata(GitMetadata value) {
        this.id = value.id;
        this.testRunId = value.testRunId;
        this.repoName = value.repoName;
        this.isMainBranch = value.isMainBranch;
        this.branchName = value.branchName;
        this.orgName = value.orgName;
        this.projectName = value.projectName;
        this.pullRequestNumber = value.pullRequestNumber;
        this.commitSha = value.commitSha;
    }

    public GitMetadata(
        Long id,
        Long testRunId,
        String repoName,
        Boolean isMainBranch,
        String branchName,
        String orgName,
        String projectName,
        Integer pullRequestNumber,
        String commitSha
    ) {
        this.id = id;
        this.testRunId = testRunId;
        this.repoName = repoName;
        this.isMainBranch = isMainBranch;
        this.branchName = branchName;
        this.orgName = orgName;
        this.projectName = projectName;
        this.pullRequestNumber = pullRequestNumber;
        this.commitSha = commitSha;
    }

    /**
     * Getter for <code>public.git_metadata.id</code>.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.git_metadata.id</code>.
     */
    public GitMetadata setId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>public.git_metadata.test_run_id</code>.
     */
    public Long getTestRunId() {
        return this.testRunId;
    }

    /**
     * Setter for <code>public.git_metadata.test_run_id</code>.
     */
    public GitMetadata setTestRunId(Long testRunId) {
        this.testRunId = testRunId;
        return this;
    }

    /**
     * Getter for <code>public.git_metadata.repo_name</code>.
     */
    public String getRepoName() {
        return this.repoName;
    }

    /**
     * Setter for <code>public.git_metadata.repo_name</code>.
     */
    public GitMetadata setRepoName(String repoName) {
        this.repoName = repoName;
        return this;
    }

    /**
     * Getter for <code>public.git_metadata.is_main_branch</code>.
     */
    public Boolean getIsMainBranch() {
        return this.isMainBranch;
    }

    /**
     * Setter for <code>public.git_metadata.is_main_branch</code>.
     */
    public GitMetadata setIsMainBranch(Boolean isMainBranch) {
        this.isMainBranch = isMainBranch;
        return this;
    }

    /**
     * Getter for <code>public.git_metadata.branch_name</code>.
     */
    public String getBranchName() {
        return this.branchName;
    }

    /**
     * Setter for <code>public.git_metadata.branch_name</code>.
     */
    public GitMetadata setBranchName(String branchName) {
        this.branchName = branchName;
        return this;
    }

    /**
     * Getter for <code>public.git_metadata.org_name</code>.
     */
    public String getOrgName() {
        return this.orgName;
    }

    /**
     * Setter for <code>public.git_metadata.org_name</code>.
     */
    public GitMetadata setOrgName(String orgName) {
        this.orgName = orgName;
        return this;
    }

    /**
     * Getter for <code>public.git_metadata.project_name</code>.
     */
    public String getProjectName() {
        return this.projectName;
    }

    /**
     * Setter for <code>public.git_metadata.project_name</code>.
     */
    public GitMetadata setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    /**
     * Getter for <code>public.git_metadata.pull_request_number</code>.
     */
    public Integer getPullRequestNumber() {
        return this.pullRequestNumber;
    }

    /**
     * Setter for <code>public.git_metadata.pull_request_number</code>.
     */
    public GitMetadata setPullRequestNumber(Integer pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber;
        return this;
    }

    /**
     * Getter for <code>public.git_metadata.commit_sha</code>.
     */
    public String getCommitSha() {
        return this.commitSha;
    }

    /**
     * Setter for <code>public.git_metadata.commit_sha</code>.
     */
    public GitMetadata setCommitSha(String commitSha) {
        this.commitSha = commitSha;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final GitMetadata other = (GitMetadata) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.testRunId == null) {
            if (other.testRunId != null)
                return false;
        }
        else if (!this.testRunId.equals(other.testRunId))
            return false;
        if (this.repoName == null) {
            if (other.repoName != null)
                return false;
        }
        else if (!this.repoName.equals(other.repoName))
            return false;
        if (this.isMainBranch == null) {
            if (other.isMainBranch != null)
                return false;
        }
        else if (!this.isMainBranch.equals(other.isMainBranch))
            return false;
        if (this.branchName == null) {
            if (other.branchName != null)
                return false;
        }
        else if (!this.branchName.equals(other.branchName))
            return false;
        if (this.orgName == null) {
            if (other.orgName != null)
                return false;
        }
        else if (!this.orgName.equals(other.orgName))
            return false;
        if (this.projectName == null) {
            if (other.projectName != null)
                return false;
        }
        else if (!this.projectName.equals(other.projectName))
            return false;
        if (this.pullRequestNumber == null) {
            if (other.pullRequestNumber != null)
                return false;
        }
        else if (!this.pullRequestNumber.equals(other.pullRequestNumber))
            return false;
        if (this.commitSha == null) {
            if (other.commitSha != null)
                return false;
        }
        else if (!this.commitSha.equals(other.commitSha))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.testRunId == null) ? 0 : this.testRunId.hashCode());
        result = prime * result + ((this.repoName == null) ? 0 : this.repoName.hashCode());
        result = prime * result + ((this.isMainBranch == null) ? 0 : this.isMainBranch.hashCode());
        result = prime * result + ((this.branchName == null) ? 0 : this.branchName.hashCode());
        result = prime * result + ((this.orgName == null) ? 0 : this.orgName.hashCode());
        result = prime * result + ((this.projectName == null) ? 0 : this.projectName.hashCode());
        result = prime * result + ((this.pullRequestNumber == null) ? 0 : this.pullRequestNumber.hashCode());
        result = prime * result + ((this.commitSha == null) ? 0 : this.commitSha.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GitMetadata (");

        sb.append(id);
        sb.append(", ").append(testRunId);
        sb.append(", ").append(repoName);
        sb.append(", ").append(isMainBranch);
        sb.append(", ").append(branchName);
        sb.append(", ").append(orgName);
        sb.append(", ").append(projectName);
        sb.append(", ").append(pullRequestNumber);
        sb.append(", ").append(commitSha);

        sb.append(")");
        return sb.toString();
    }
}
