-- Claims a results group in a repo for one test run, so concurrent first uploads with the same group
-- share a test run. Inserted before the test run is saved, so it doesn't reference the test_run table.
CREATE TABLE test_run_group_claim(
    repo_name           text        not null,
    group_name          text        not null,
    test_run_public_id  varchar(12) not null,
    created_timestamp   timestamp   not null default now(),
    PRIMARY KEY (repo_name, group_name)
);
CREATE INDEX idx_test_run_group_claim_public_id on test_run_group_claim(test_run_public_id);
