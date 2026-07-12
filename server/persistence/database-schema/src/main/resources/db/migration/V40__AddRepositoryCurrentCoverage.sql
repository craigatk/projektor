CREATE TABLE repository_current_coverage(
    id                  bigserial primary key,
    repo_name           text not null,
    project_name        text not null default '',
    branch_name         text,
    covered_percentage  numeric not null,
    test_run_public_id  varchar(12) not null,
    created_timestamp   timestamp not null,
    updated_timestamp   timestamp not null default now(),
    unique (repo_name, project_name)
);

CREATE INDEX repository_current_coverage_repo_name_idx on repository_current_coverage(repo_name);
