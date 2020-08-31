CREATE TABLE git_metadata(
    id             bigserial primary key,
    test_run_id    bigint references test_run ON DELETE CASCADE,
    repo_name      text,
    is_main_branch boolean not null,
    branch_name    text
);

CREATE INDEX git_metadata_test_run_id_idx on git_metadata(test_run_id);
CREATE INDEX git_metadata_repo_name_idx on git_metadata(repo_name);
CREATE INDEX git_metadata_is_main_branch_idx on git_metadata(is_main_branch);
