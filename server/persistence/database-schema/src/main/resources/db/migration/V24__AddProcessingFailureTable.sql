CREATE TABLE processing_failure(
    id                bigserial primary key,
    public_id         varchar(12) references test_run(public_id) ON DELETE CASCADE,
    body              text,
    failure           text,
    body_type         text not null,
    created_timestamp TIMESTAMP not null
);

CREATE INDEX idx_processing_failure_public_id on processing_failure(public_id);
