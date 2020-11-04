CREATE TABLE performance_results(
    id                  bigserial primary key,
    test_run_id         bigint references test_run ON DELETE CASCADE,
    test_run_public_id  varchar(12),
    name                text,
    request_count       bigint,
    requests_per_second numeric(12, 3) not null,
    average             numeric(12, 3) not null,
    maximum             numeric(12, 3) not null,
    p95                 numeric(12, 3) not null
);

CREATE INDEX performance_results_run_id_idx on performance_results(test_run_id);
CREATE INDEX performance_results_public_id_idx on performance_results(test_run_public_id);
