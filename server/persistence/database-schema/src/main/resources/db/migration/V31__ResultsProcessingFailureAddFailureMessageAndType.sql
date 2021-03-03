ALTER TABLE results_processing_failure ADD COLUMN failure_message TEXT;

ALTER TABLE results_processing_failure ADD COLUMN failure_type TEXT;

ALTER TABLE results_processing_failure ADD COLUMN body_type TEXT;
CREATE INDEX results_processing_failure_body_type_idx ON results_processing_failure(body_type);

CREATE INDEX results_processing_failure_created_timestamp_idx ON results_processing_failure(created_timestamp);