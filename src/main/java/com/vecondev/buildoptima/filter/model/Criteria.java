package com.vecondev.buildoptima.filter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Criteria {

    public static final String OPERATION = "operation";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String VALUES = "values";

    private SearchOperation operation;

    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> values;

    public Criteria(SearchOperation operation, String name, String value) {
        this.operation = operation;
        this.name = name;
        this.value = value;
    }
}
