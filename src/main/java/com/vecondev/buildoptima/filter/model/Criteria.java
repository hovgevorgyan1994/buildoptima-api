package com.vecondev.buildoptima.filter.model;

import lombok.Data;

import java.util.List;

@Data
public class Criteria {

    public static final String OPERATION = "operation";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String VALUES = "values";

    private SearchOperation operation;

    private String name;

    private String value;

    private List<String> values;



}
