CREATE TABLE results_metadata(
    id          bigserial primary key,
    test_run_id bigint references test_run ON DELETE CASCADE,
    ci          boolean
);

CREATE INDEX results_metadata_test_run_id_idx on results_metadata(test_run_id);
