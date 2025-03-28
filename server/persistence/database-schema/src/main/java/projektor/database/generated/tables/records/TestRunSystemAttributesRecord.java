/*
 * This file is generated by jOOQ.
 */
package projektor.database.generated.tables.records;


import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;

import projektor.database.generated.tables.TestRunSystemAttributes;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class TestRunSystemAttributesRecord extends UpdatableRecordImpl<TestRunSystemAttributesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for
     * <code>public.test_run_system_attributes.test_run_public_id</code>.
     */
    public TestRunSystemAttributesRecord setTestRunPublicId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for
     * <code>public.test_run_system_attributes.test_run_public_id</code>.
     */
    public String getTestRunPublicId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.test_run_system_attributes.pinned</code>.
     */
    public TestRunSystemAttributesRecord setPinned(Boolean value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.test_run_system_attributes.pinned</code>.
     */
    public Boolean getPinned() {
        return (Boolean) get(1);
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
     * Create a detached TestRunSystemAttributesRecord
     */
    public TestRunSystemAttributesRecord() {
        super(TestRunSystemAttributes.TEST_RUN_SYSTEM_ATTRIBUTES);
    }

    /**
     * Create a detached, initialised TestRunSystemAttributesRecord
     */
    public TestRunSystemAttributesRecord(String testRunPublicId, Boolean pinned) {
        super(TestRunSystemAttributes.TEST_RUN_SYSTEM_ATTRIBUTES);

        setTestRunPublicId(testRunPublicId);
        setPinned(pinned);
        resetTouchedOnNotNull();
    }

    /**
     * Create a detached, initialised TestRunSystemAttributesRecord
     */
    public TestRunSystemAttributesRecord(projektor.database.generated.tables.pojos.TestRunSystemAttributes value) {
        super(TestRunSystemAttributes.TEST_RUN_SYSTEM_ATTRIBUTES);

        if (value != null) {
            setTestRunPublicId(value.getTestRunPublicId());
            setPinned(value.getPinned());
            resetTouchedOnNotNull();
        }
    }
}
