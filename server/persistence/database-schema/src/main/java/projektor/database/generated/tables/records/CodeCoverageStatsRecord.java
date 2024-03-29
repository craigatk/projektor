/*
 * This file is generated by jOOQ.
 */
package projektor.database.generated.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;

import projektor.database.generated.tables.CodeCoverageStats;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CodeCoverageStatsRecord extends UpdatableRecordImpl<CodeCoverageStatsRecord> implements Record9<Long, Long, String, Integer, Integer, Integer, Integer, Integer, Integer> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.code_coverage_stats.id</code>.
     */
    public CodeCoverageStatsRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.code_coverage_stats.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.code_coverage_stats.code_coverage_run_id</code>.
     */
    public CodeCoverageStatsRecord setCodeCoverageRunId(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.code_coverage_stats.code_coverage_run_id</code>.
     */
    public Long getCodeCoverageRunId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.code_coverage_stats.scope</code>.
     */
    public CodeCoverageStatsRecord setScope(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.code_coverage_stats.scope</code>.
     */
    public String getScope() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.code_coverage_stats.statement_covered</code>.
     */
    public CodeCoverageStatsRecord setStatementCovered(Integer value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.code_coverage_stats.statement_covered</code>.
     */
    public Integer getStatementCovered() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>public.code_coverage_stats.statement_missed</code>.
     */
    public CodeCoverageStatsRecord setStatementMissed(Integer value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.code_coverage_stats.statement_missed</code>.
     */
    public Integer getStatementMissed() {
        return (Integer) get(4);
    }

    /**
     * Setter for <code>public.code_coverage_stats.line_covered</code>.
     */
    public CodeCoverageStatsRecord setLineCovered(Integer value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>public.code_coverage_stats.line_covered</code>.
     */
    public Integer getLineCovered() {
        return (Integer) get(5);
    }

    /**
     * Setter for <code>public.code_coverage_stats.line_missed</code>.
     */
    public CodeCoverageStatsRecord setLineMissed(Integer value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>public.code_coverage_stats.line_missed</code>.
     */
    public Integer getLineMissed() {
        return (Integer) get(6);
    }

    /**
     * Setter for <code>public.code_coverage_stats.branch_covered</code>.
     */
    public CodeCoverageStatsRecord setBranchCovered(Integer value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>public.code_coverage_stats.branch_covered</code>.
     */
    public Integer getBranchCovered() {
        return (Integer) get(7);
    }

    /**
     * Setter for <code>public.code_coverage_stats.branch_missed</code>.
     */
    public CodeCoverageStatsRecord setBranchMissed(Integer value) {
        set(8, value);
        return this;
    }

    /**
     * Getter for <code>public.code_coverage_stats.branch_missed</code>.
     */
    public Integer getBranchMissed() {
        return (Integer) get(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row9<Long, Long, String, Integer, Integer, Integer, Integer, Integer, Integer> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    @Override
    public Row9<Long, Long, String, Integer, Integer, Integer, Integer, Integer, Integer> valuesRow() {
        return (Row9) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return CodeCoverageStats.CODE_COVERAGE_STATS.ID;
    }

    @Override
    public Field<Long> field2() {
        return CodeCoverageStats.CODE_COVERAGE_STATS.CODE_COVERAGE_RUN_ID;
    }

    @Override
    public Field<String> field3() {
        return CodeCoverageStats.CODE_COVERAGE_STATS.SCOPE;
    }

    @Override
    public Field<Integer> field4() {
        return CodeCoverageStats.CODE_COVERAGE_STATS.STATEMENT_COVERED;
    }

    @Override
    public Field<Integer> field5() {
        return CodeCoverageStats.CODE_COVERAGE_STATS.STATEMENT_MISSED;
    }

    @Override
    public Field<Integer> field6() {
        return CodeCoverageStats.CODE_COVERAGE_STATS.LINE_COVERED;
    }

    @Override
    public Field<Integer> field7() {
        return CodeCoverageStats.CODE_COVERAGE_STATS.LINE_MISSED;
    }

    @Override
    public Field<Integer> field8() {
        return CodeCoverageStats.CODE_COVERAGE_STATS.BRANCH_COVERED;
    }

    @Override
    public Field<Integer> field9() {
        return CodeCoverageStats.CODE_COVERAGE_STATS.BRANCH_MISSED;
    }

    @Override
    public Long component1() {
        return getId();
    }

    @Override
    public Long component2() {
        return getCodeCoverageRunId();
    }

    @Override
    public String component3() {
        return getScope();
    }

    @Override
    public Integer component4() {
        return getStatementCovered();
    }

    @Override
    public Integer component5() {
        return getStatementMissed();
    }

    @Override
    public Integer component6() {
        return getLineCovered();
    }

    @Override
    public Integer component7() {
        return getLineMissed();
    }

    @Override
    public Integer component8() {
        return getBranchCovered();
    }

    @Override
    public Integer component9() {
        return getBranchMissed();
    }

    @Override
    public Long value1() {
        return getId();
    }

    @Override
    public Long value2() {
        return getCodeCoverageRunId();
    }

    @Override
    public String value3() {
        return getScope();
    }

    @Override
    public Integer value4() {
        return getStatementCovered();
    }

    @Override
    public Integer value5() {
        return getStatementMissed();
    }

    @Override
    public Integer value6() {
        return getLineCovered();
    }

    @Override
    public Integer value7() {
        return getLineMissed();
    }

    @Override
    public Integer value8() {
        return getBranchCovered();
    }

    @Override
    public Integer value9() {
        return getBranchMissed();
    }

    @Override
    public CodeCoverageStatsRecord value1(Long value) {
        setId(value);
        return this;
    }

    @Override
    public CodeCoverageStatsRecord value2(Long value) {
        setCodeCoverageRunId(value);
        return this;
    }

    @Override
    public CodeCoverageStatsRecord value3(String value) {
        setScope(value);
        return this;
    }

    @Override
    public CodeCoverageStatsRecord value4(Integer value) {
        setStatementCovered(value);
        return this;
    }

    @Override
    public CodeCoverageStatsRecord value5(Integer value) {
        setStatementMissed(value);
        return this;
    }

    @Override
    public CodeCoverageStatsRecord value6(Integer value) {
        setLineCovered(value);
        return this;
    }

    @Override
    public CodeCoverageStatsRecord value7(Integer value) {
        setLineMissed(value);
        return this;
    }

    @Override
    public CodeCoverageStatsRecord value8(Integer value) {
        setBranchCovered(value);
        return this;
    }

    @Override
    public CodeCoverageStatsRecord value9(Integer value) {
        setBranchMissed(value);
        return this;
    }

    @Override
    public CodeCoverageStatsRecord values(Long value1, Long value2, String value3, Integer value4, Integer value5, Integer value6, Integer value7, Integer value8, Integer value9) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CodeCoverageStatsRecord
     */
    public CodeCoverageStatsRecord() {
        super(CodeCoverageStats.CODE_COVERAGE_STATS);
    }

    /**
     * Create a detached, initialised CodeCoverageStatsRecord
     */
    public CodeCoverageStatsRecord(Long id, Long codeCoverageRunId, String scope, Integer statementCovered, Integer statementMissed, Integer lineCovered, Integer lineMissed, Integer branchCovered, Integer branchMissed) {
        super(CodeCoverageStats.CODE_COVERAGE_STATS);

        setId(id);
        setCodeCoverageRunId(codeCoverageRunId);
        setScope(scope);
        setStatementCovered(statementCovered);
        setStatementMissed(statementMissed);
        setLineCovered(lineCovered);
        setLineMissed(lineMissed);
        setBranchCovered(branchCovered);
        setBranchMissed(branchMissed);
    }

    /**
     * Create a detached, initialised CodeCoverageStatsRecord
     */
    public CodeCoverageStatsRecord(projektor.database.generated.tables.pojos.CodeCoverageStats value) {
        super(CodeCoverageStats.CODE_COVERAGE_STATS);

        if (value != null) {
            setId(value.getId());
            setCodeCoverageRunId(value.getCodeCoverageRunId());
            setScope(value.getScope());
            setStatementCovered(value.getStatementCovered());
            setStatementMissed(value.getStatementMissed());
            setLineCovered(value.getLineCovered());
            setLineMissed(value.getLineMissed());
            setBranchCovered(value.getBranchCovered());
            setBranchMissed(value.getBranchMissed());
        }
    }
}
