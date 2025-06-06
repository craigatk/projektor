/*
 * This file is generated by jOOQ.
 */
package projektor.database.generated.tables.records;


import java.time.LocalDateTime;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;

import projektor.database.generated.tables.ResultsProcessing;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class ResultsProcessingRecord extends UpdatableRecordImpl<ResultsProcessingRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.results_processing.public_id</code>.
     */
    public ResultsProcessingRecord setPublicId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.results_processing.public_id</code>.
     */
    public String getPublicId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.results_processing.status</code>.
     */
    public ResultsProcessingRecord setStatus(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.results_processing.status</code>.
     */
    public String getStatus() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.results_processing.error_message</code>.
     */
    public ResultsProcessingRecord setErrorMessage(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.results_processing.error_message</code>.
     */
    public String getErrorMessage() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.results_processing.created_timestamp</code>.
     */
    public ResultsProcessingRecord setCreatedTimestamp(LocalDateTime value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.results_processing.created_timestamp</code>.
     */
    public LocalDateTime getCreatedTimestamp() {
        return (LocalDateTime) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ResultsProcessingRecord
     */
    public ResultsProcessingRecord() {
        super(ResultsProcessing.RESULTS_PROCESSING);
    }

    /**
     * Create a detached, initialised ResultsProcessingRecord
     */
    public ResultsProcessingRecord(String publicId, String status, String errorMessage, LocalDateTime createdTimestamp) {
        super(ResultsProcessing.RESULTS_PROCESSING);

        setPublicId(publicId);
        setStatus(status);
        setErrorMessage(errorMessage);
        setCreatedTimestamp(createdTimestamp);
        resetTouchedOnNotNull();
    }

    /**
     * Create a detached, initialised ResultsProcessingRecord
     */
    public ResultsProcessingRecord(projektor.database.generated.tables.pojos.ResultsProcessing value) {
        super(ResultsProcessing.RESULTS_PROCESSING);

        if (value != null) {
            setPublicId(value.getPublicId());
            setStatus(value.getStatus());
            setErrorMessage(value.getErrorMessage());
            setCreatedTimestamp(value.getCreatedTimestamp());
            resetTouchedOnNotNull();
        }
    }
}
