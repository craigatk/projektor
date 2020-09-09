ALTER TABLE git_metadata ADD COLUMN org_name text;

CREATE TABLE git_repository(
    repo_name text primary key,
    org_name text not null
);

CREATE INDEX git_repository_org_name_idx on git_repository(org_name);
