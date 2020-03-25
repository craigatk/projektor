UPDATE test_run SET created_timestamp = NOW() - INTERVAL '60 DAY' where created_timestamp is null;

ALTER TABLE test_run ALTER COLUMN created_timestamp SET NOT NULL;
