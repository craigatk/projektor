CREATE TABLE repository_slack_channel(
    id                 bigserial primary key,
    repo_name          text not null,
    project_name       text not null default '',
    channel_name       text not null,
    created_timestamp  timestamp not null default now(),
    updated_timestamp  timestamp not null default now(),
    unique (repo_name, project_name)
);
