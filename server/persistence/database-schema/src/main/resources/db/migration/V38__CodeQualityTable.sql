CREATE TABLE code_quality_report(
  id            bigserial primary key,
  test_run_id   bigint references test_run ON DELETE CASCADE,
  contents      text,
  file_name     text,
  group_name    text
);
