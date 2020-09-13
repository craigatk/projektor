ALTER TABLE git_metadata ADD COLUMN project_name TEXT;

CREATE INDEX git_metadata_project_name_idx on git_metadata(project_name);
