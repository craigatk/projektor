CREATE TABLE test_suite_group(
  id          bigserial primary key,
  test_run_id bigint references test_run ON DELETE CASCADE,
  group_name  text,
  group_label text,
  directory   text
);

ALTER TABLE test_suite ADD COLUMN test_suite_group_id bigint references test_suite_group ON DELETE CASCADE;
