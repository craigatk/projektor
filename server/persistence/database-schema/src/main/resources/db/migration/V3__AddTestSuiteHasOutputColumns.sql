ALTER TABLE test_suite ADD COLUMN has_system_out boolean not null default false;
ALTER TABLE test_suite ADD COLUMN has_system_err boolean not null default false;