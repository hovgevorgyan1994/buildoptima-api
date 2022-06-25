package com.vecondev.buildoptima.filter.model;

import com.vecondev.buildoptima.filter.converter.InstantConverter;
import com.vecondev.buildoptima.filter.converter.RoleConverter;
import com.vecondev.buildoptima.model.user.User;

import java.util.Map;

import static com.vecondev.buildoptima.filter.model.FieldType.DATETIME;
import static com.vecondev.buildoptima.filter.model.FieldType.STRING;

public class UserFields {

  private UserFields() {}

  public static final Map<String, FieldDefinition<User, ?>> userPageSortingFieldsMap;
  private static final String FIRST_NAME = "firstName";
  private static final String LAST_NAME = "lastName";
  private static final String EMAIL = "email";
  private static final String PHONE = "phone";
  private static final String ROLE = "role";
  private static final String CREATION_DATE = "creationDate";

  static {
    userPageSortingFieldsMap =
        Map.of(
            FIRST_NAME,
            new FieldDefinition<User, String>(FIRST_NAME, STRING, FIRST_NAME),
            LAST_NAME,
            new FieldDefinition<User, String>(LAST_NAME, STRING, LAST_NAME),
            EMAIL,
            new FieldDefinition<User, String>(EMAIL, STRING, EMAIL),
            PHONE,
            new FieldDefinition<User, String>(PHONE, STRING, PHONE),
            ROLE,
            new FieldDefinition<>(ROLE, STRING, ROLE, new RoleConverter()),
            CREATION_DATE,
            new FieldDefinition<>(CREATION_DATE, DATETIME, CREATION_DATE, new InstantConverter()));
  }
}
