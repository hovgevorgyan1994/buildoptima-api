package com.vecondev.buildoptima.filter.model;

import lombok.Getter;

import java.util.Set;

import static com.vecondev.buildoptima.filter.model.SearchOperation.*;

@Getter
public enum FieldType {
  STRING(Set.of(EQ, LIKE, NE, IN)),
  DOUBLE(Set.of(GT, GE, EQ, LE, LT, IN, NE)),
  LONG(Set.of(GT, GE, EQ, LE, LT, IN, NE)),
  BOOLEAN(Set.of(EQ, NE)),
  DATE(Set.of(GT, GE, EQ, LE, LT, IN, NE)),
  DATETIME(Set.of(GT, GE, EQ, LE, LT, IN, NE));

  private final Set<SearchOperation> operations;

  FieldType(Set<SearchOperation> operations) {
    this.operations = operations;
  }
}
