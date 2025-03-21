/*
 * This file is generated by jOOQ.
 */
package projektor.database.generated.tables.records;


import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;

import projektor.database.generated.tables.CodeQualityReport;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class CodeQualityReportRecord extends UpdatableRecordImpl<CodeQualityReportRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.code_quality_report.id</code>.
     */
    public CodeQualityReportRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.code_quality_report.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.code_quality_report.test_run_id</code>.
     */
    public CodeQualityReportRecord setTestRunId(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.code_quality_report.test_run_id</code>.
     */
    public Long getTestRunId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.code_quality_report.contents</code>.
     */
    public CodeQualityReportRecord setContents(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.code_quality_report.contents</code>.
     */
    public String getContents() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.code_quality_report.file_name</code>.
     */
    public CodeQualityReportRecord setFileName(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.code_quality_report.file_name</code>.
     */
    public String getFileName() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.code_quality_report.group_name</code>.
     */
    public CodeQualityReportRecord setGroupName(String value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.code_quality_report.group_name</code>.
     */
    public String getGroupName() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.code_quality_report.idx</code>.
     */
    public CodeQualityReportRecord setIdx(Integer value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>public.code_quality_report.idx</code>.
     */
    public Integer getIdx() {
        return (Integer) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CodeQualityReportRecord
     */
    public CodeQualityReportRecord() {
        super(CodeQualityReport.CODE_QUALITY_REPORT);
    }

    /**
     * Create a detached, initialised CodeQualityReportRecord
     */
    public CodeQualityReportRecord(Long id, Long testRunId, String contents, String fileName, String groupName, Integer idx) {
        super(CodeQualityReport.CODE_QUALITY_REPORT);

        setId(id);
        setTestRunId(testRunId);
        setContents(contents);
        setFileName(fileName);
        setGroupName(groupName);
        setIdx(idx);
        resetTouchedOnNotNull();
    }

    /**
     * Create a detached, initialised CodeQualityReportRecord
     */
    public CodeQualityReportRecord(projektor.database.generated.tables.pojos.CodeQualityReport value) {
        super(CodeQualityReport.CODE_QUALITY_REPORT);

        if (value != null) {
            setId(value.getId());
            setTestRunId(value.getTestRunId());
            setContents(value.getContents());
            setFileName(value.getFileName());
            setGroupName(value.getGroupName());
            setIdx(value.getIdx());
            resetTouchedOnNotNull();
        }
    }
}
