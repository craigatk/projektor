package projektor.parser.coverage.clover.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Coverage {
    public Project project;
}
