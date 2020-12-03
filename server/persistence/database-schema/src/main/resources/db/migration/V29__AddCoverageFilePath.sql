ALTER TABLE code_coverage_group DROP COLUMN base_directory_path;

ALTER TABLE code_coverage_file ADD COLUMN file_path text;
