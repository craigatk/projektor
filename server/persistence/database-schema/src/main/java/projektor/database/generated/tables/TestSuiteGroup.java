/*
 * This file is generated by jOOQ.
 */
package projektor.database.generated.tables;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import projektor.database.generated.Keys;
import projektor.database.generated.Public;
import projektor.database.generated.tables.TestRun.TestRunPath;
import projektor.database.generated.tables.TestSuite.TestSuitePath;
import projektor.database.generated.tables.records.TestSuiteGroupRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class TestSuiteGroup extends TableImpl<TestSuiteGroupRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.test_suite_group</code>
     */
    public static final TestSuiteGroup TEST_SUITE_GROUP = new TestSuiteGroup();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TestSuiteGroupRecord> getRecordType() {
        return TestSuiteGroupRecord.class;
    }

    /**
     * The column <code>public.test_suite_group.id</code>.
     */
    public final TableField<TestSuiteGroupRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.test_suite_group.test_run_id</code>.
     */
    public final TableField<TestSuiteGroupRecord, Long> TEST_RUN_ID = createField(DSL.name("test_run_id"), SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.test_suite_group.group_name</code>.
     */
    public final TableField<TestSuiteGroupRecord, String> GROUP_NAME = createField(DSL.name("group_name"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.test_suite_group.group_label</code>.
     */
    public final TableField<TestSuiteGroupRecord, String> GROUP_LABEL = createField(DSL.name("group_label"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.test_suite_group.directory</code>.
     */
    public final TableField<TestSuiteGroupRecord, String> DIRECTORY = createField(DSL.name("directory"), SQLDataType.CLOB, this, "");

    private TestSuiteGroup(Name alias, Table<TestSuiteGroupRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private TestSuiteGroup(Name alias, Table<TestSuiteGroupRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.test_suite_group</code> table reference
     */
    public TestSuiteGroup(String alias) {
        this(DSL.name(alias), TEST_SUITE_GROUP);
    }

    /**
     * Create an aliased <code>public.test_suite_group</code> table reference
     */
    public TestSuiteGroup(Name alias) {
        this(alias, TEST_SUITE_GROUP);
    }

    /**
     * Create a <code>public.test_suite_group</code> table reference
     */
    public TestSuiteGroup() {
        this(DSL.name("test_suite_group"), null);
    }

    public <O extends Record> TestSuiteGroup(Table<O> path, ForeignKey<O, TestSuiteGroupRecord> childPath, InverseForeignKey<O, TestSuiteGroupRecord> parentPath) {
        super(path, childPath, parentPath, TEST_SUITE_GROUP);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class TestSuiteGroupPath extends TestSuiteGroup implements Path<TestSuiteGroupRecord> {

        private static final long serialVersionUID = 1L;
        public <O extends Record> TestSuiteGroupPath(Table<O> path, ForeignKey<O, TestSuiteGroupRecord> childPath, InverseForeignKey<O, TestSuiteGroupRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private TestSuiteGroupPath(Name alias, Table<TestSuiteGroupRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public TestSuiteGroupPath as(String alias) {
            return new TestSuiteGroupPath(DSL.name(alias), this);
        }

        @Override
        public TestSuiteGroupPath as(Name alias) {
            return new TestSuiteGroupPath(alias, this);
        }

        @Override
        public TestSuiteGroupPath as(Table<?> alias) {
            return new TestSuiteGroupPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<TestSuiteGroupRecord, Long> getIdentity() {
        return (Identity<TestSuiteGroupRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<TestSuiteGroupRecord> getPrimaryKey() {
        return Keys.TEST_SUITE_GROUP_PKEY;
    }

    @Override
    public List<ForeignKey<TestSuiteGroupRecord, ?>> getReferences() {
        return Arrays.asList(Keys.TEST_SUITE_GROUP__TEST_SUITE_GROUP_TEST_RUN_ID_FKEY);
    }

    private transient TestRunPath _testRun;

    /**
     * Get the implicit join path to the <code>public.test_run</code> table.
     */
    public TestRunPath testRun() {
        if (_testRun == null)
            _testRun = new TestRunPath(this, Keys.TEST_SUITE_GROUP__TEST_SUITE_GROUP_TEST_RUN_ID_FKEY, null);

        return _testRun;
    }

    private transient TestSuitePath _testSuite;

    /**
     * Get the implicit to-many join path to the <code>public.test_suite</code>
     * table
     */
    public TestSuitePath testSuite() {
        if (_testSuite == null)
            _testSuite = new TestSuitePath(this, null, Keys.TEST_SUITE__TEST_SUITE_TEST_SUITE_GROUP_ID_FKEY.getInverseKey());

        return _testSuite;
    }

    @Override
    public TestSuiteGroup as(String alias) {
        return new TestSuiteGroup(DSL.name(alias), this);
    }

    @Override
    public TestSuiteGroup as(Name alias) {
        return new TestSuiteGroup(alias, this);
    }

    @Override
    public TestSuiteGroup as(Table<?> alias) {
        return new TestSuiteGroup(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public TestSuiteGroup rename(String name) {
        return new TestSuiteGroup(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TestSuiteGroup rename(Name name) {
        return new TestSuiteGroup(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public TestSuiteGroup rename(Table<?> name) {
        return new TestSuiteGroup(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TestSuiteGroup where(Condition condition) {
        return new TestSuiteGroup(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TestSuiteGroup where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TestSuiteGroup where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TestSuiteGroup where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public TestSuiteGroup where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public TestSuiteGroup where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public TestSuiteGroup where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public TestSuiteGroup where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TestSuiteGroup whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public TestSuiteGroup whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
