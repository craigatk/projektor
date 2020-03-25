CREATE TABLE test_run(
  id                  bigserial primary key,
  public_id           varchar(12) unique,
  total_test_count    integer not null,
  total_passing_count integer not null,
  total_skipped_count integer not null,
  total_failure_count integer not null,
  passed              boolean not null,
  cumulative_duration numeric(12, 3) not null
);
CREATE INDEX idx_test_run_public_id on test_run(public_id);

CREATE TABLE test_suite(
  id            bigserial primary key,
  test_run_id   bigserial references test_run ON DELETE CASCADE,
  idx           integer   not null,
  package_name  text,
  class_name    text      not null,
  test_count    integer   not null,
  passing_count integer   not null,
  skipped_count integer   not null,
  failure_count integer   not null,
  start_ts      timestamp,
  hostname      text,
  duration      numeric(12, 3) not null,
  system_out    text,
  system_err    text
);

CREATE INDEX idx_test_suite_idx on test_suite(idx);

CREATE TABLE test_case(
  id            bigserial      primary key,
  test_suite_id bigserial      references test_suite ON DELETE CASCADE,
  idx           integer        not null,
  name          text           not null,
  package_name  text,
  class_name    text           not null,
  duration      numeric(12, 3) not null,
  passed        boolean        not null,
  skipped       boolean        not null
);

CREATE INDEX idx_test_case_idx on test_case(idx);
CREATE INDEX idx_test_case_passed on test_case(passed);

CREATE TABLE test_failure(
  id              bigserial primary key,
  test_case_id    bigserial references test_case ON DELETE CASCADE,
  failure_message text,
  failure_type    text,
  failure_text    text
);
