package projektor.parser.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCase {
    public String name;

    @JsonProperty("classname")
    public String className;

    public BigDecimal time;

    @JsonSetter(nulls= Nulls.AS_EMPTY)
    public Skipped skipped;

    public Failure failure;
}
