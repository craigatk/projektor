CREATE TABLE results_processing(
    public_id     varchar(12) primary key,
    status        varchar(20) not null,
    error_message text
);
CREATE INDEX idx_results_processing_status on results_processing(status);
