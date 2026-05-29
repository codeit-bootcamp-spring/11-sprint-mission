package com.sprint.mission.discodeit.controller.api.examples;

public class AuthExamples {

  public static final String ERROR_400 = """
      {
        "code": "COMMON_001",
        "exceptionType": "VALIDATION_ERROR",
        "message": "request validation failed.",
        "fields": ["username: must not be blank", "password: must not be blank"]
      }
      """;

  public static final String ERROR_401_AUTH_001 = """
      {
        "code": "AUTH_001",
        "exceptionType": "INVALID_CREDENTIALS",
        "message": "password is not matched."
      }
      """;

  public static final String ERROR_404_USER_003 = """
      {
        "code": "USER_003",
        "exceptionType": "ENTITY_NOT_FOUND",
        "message": "requested user not found."
      }
      """;
}