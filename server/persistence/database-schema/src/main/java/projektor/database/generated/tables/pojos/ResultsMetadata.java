/*
 * This file is generated by jOOQ.
 */
package projektor.database.generated.tables.pojos;


import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class ResultsMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long testRunId;
    private Boolean ci;
    private String group;

    public ResultsMetadata() {}

    public ResultsMetadata(ResultsMetadata value) {
        this.id = value.id;
        this.testRunId = value.testRunId;
        this.ci = value.ci;
        this.group = value.group;
    }

    public ResultsMetadata(
        Long id,
        Long testRunId,
        Boolean ci,
        String group
    ) {
        this.id = id;
        this.testRunId = testRunId;
        this.ci = ci;
        this.group = group;
    }

    /**
     * Getter for <code>public.results_metadata.id</code>.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.results_metadata.id</code>.
     */
    public ResultsMetadata setId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>public.results_metadata.test_run_id</code>.
     */
    public Long getTestRunId() {
        return this.testRunId;
    }

    /**
     * Setter for <code>public.results_metadata.test_run_id</code>.
     */
    public ResultsMetadata setTestRunId(Long testRunId) {
        this.testRunId = testRunId;
        return this;
    }

    /**
     * Getter for <code>public.results_metadata.ci</code>.
     */
    public Boolean getCi() {
        return this.ci;
    }

    /**
     * Setter for <code>public.results_metadata.ci</code>.
     */
    public ResultsMetadata setCi(Boolean ci) {
        this.ci = ci;
        return this;
    }

    /**
     * Getter for <code>public.results_metadata.group</code>.
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Setter for <code>public.results_metadata.group</code>.
     */
    public ResultsMetadata setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ResultsMetadata other = (ResultsMetadata) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.testRunId == null) {
            if (other.testRunId != null)
                return false;
        }
        else if (!this.testRunId.equals(other.testRunId))
            return false;
        if (this.ci == null) {
            if (other.ci != null)
                return false;
        }
        else if (!this.ci.equals(other.ci))
            return false;
        if (this.group == null) {
            if (other.group != null)
                return false;
        }
        else if (!this.group.equals(other.group))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.testRunId == null) ? 0 : this.testRunId.hashCode());
        result = prime * result + ((this.ci == null) ? 0 : this.ci.hashCode());
        result = prime * result + ((this.group == null) ? 0 : this.group.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ResultsMetadata (");

        sb.append(id);
        sb.append(", ").append(testRunId);
        sb.append(", ").append(ci);
        sb.append(", ").append(group);

        sb.append(")");
        return sb.toString();
    }
}
