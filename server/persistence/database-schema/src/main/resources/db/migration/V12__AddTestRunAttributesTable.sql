CREATE TABLE test_run_system_attributes(
     test_run_public_id  varchar(12) primary key references test_run(public_id) ON DELETE CASCADE,
     pinned              boolean not null
);