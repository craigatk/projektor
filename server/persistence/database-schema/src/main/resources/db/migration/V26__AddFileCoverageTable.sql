CREATE TABLE code_coverage_file(
    id                     bigserial primary key,
    code_coverage_run_id   bigint references code_coverage_run ON DELETE CASCADE,
    code_coverage_group_id bigint references code_coverage_group,
    stats_id               bigint references code_coverage_stats,
    directory_name         text,
    file_name              text,
    missed_lines           integer[],
    partial_lines          integer[]
);

CREATE INDEX code_coverage_file_code_coverage_run_id_idx on code_coverage_file(code_coverage_run_id);
CREATE INDEX code_coverage_file_code_coverage_group_id_idx on code_coverage_file(code_coverage_group_id);
