/*
 * This file is generated by jOOQ.
 */
package projektor.database.generated.tables.records;


import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;

import projektor.database.generated.tables.GitRepository;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class GitRepositoryRecord extends UpdatableRecordImpl<GitRepositoryRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.git_repository.repo_name</code>.
     */
    public GitRepositoryRecord setRepoName(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.git_repository.repo_name</code>.
     */
    public String getRepoName() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.git_repository.org_name</code>.
     */
    public GitRepositoryRecord setOrgName(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.git_repository.org_name</code>.
     */
    public String getOrgName() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached GitRepositoryRecord
     */
    public GitRepositoryRecord() {
        super(GitRepository.GIT_REPOSITORY);
    }

    /**
     * Create a detached, initialised GitRepositoryRecord
     */
    public GitRepositoryRecord(String repoName, String orgName) {
        super(GitRepository.GIT_REPOSITORY);

        setRepoName(repoName);
        setOrgName(orgName);
        resetTouchedOnNotNull();
    }

    /**
     * Create a detached, initialised GitRepositoryRecord
     */
    public GitRepositoryRecord(projektor.database.generated.tables.pojos.GitRepository value) {
        super(GitRepository.GIT_REPOSITORY);

        if (value != null) {
            setRepoName(value.getRepoName());
            setOrgName(value.getOrgName());
            resetTouchedOnNotNull();
        }
    }
}
