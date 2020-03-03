ALTER TABLE test_run DROP COLUMN has_attachments;

ALTER TABLE test_run_attachment DROP COLUMN test_run_id;

ALTER TABLE test_run_attachment ADD COLUMN test_run_public_id varchar(12);
CREATE INDEX idx_attachment_test_run_id on test_run_attachment(test_run_public_id);
