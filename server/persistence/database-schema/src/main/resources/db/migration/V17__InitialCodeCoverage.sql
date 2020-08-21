CREATE TABLE code_coverage_run(
  id                 bigserial primary key,
  test_run_public_id varchar(12)
);

CREATE INDEX code_coverage_run_test_run_public_id_idx on code_coverage_run(test_run_public_id);

CREATE TABLE code_coverage_stats(
  id                   bigserial primary key,
  code_coverage_run_id bigint references code_coverage_run ON DELETE CASCADE,
  scope                text not null,
  statement_covered    int not null,
  statement_missed     int not null,
  line_covered         int not null,
  line_missed          int not null,
  branch_covered       int not null,
  branch_missed        int not null
);

CREATE INDEX code_coverage_stats_code_coverage_run_id_idx on code_coverage_stats(code_coverage_run_id);

CREATE TABLE code_coverage_group(
  id                   bigserial primary key,
  code_coverage_run_id bigint references code_coverage_run ON DELETE CASCADE,
  name                 text,
  stats_id             bigint references code_coverage_stats
);

CREATE INDEX code_coverage_group_code_coverage_run_id_idx on code_coverage_group(code_coverage_run_id);
