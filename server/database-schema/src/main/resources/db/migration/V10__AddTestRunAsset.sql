CREATE TABLE test_run_asset(
   id          bigserial primary key,
   test_run_id bigint references test_run ON DELETE CASCADE,
   file_name   text NOT NULL,
   bucket_name text NOT NULL,
   file_size   bigint
);

ALTER TABLE test_run ADD COLUMN has_assets boolean NOT NULL DEFAULT false;

CREATE TABLE test_run_access(
    test_run_id bigint primary key references test_run ON DELETE CASCADE,
    asset_key text
)
