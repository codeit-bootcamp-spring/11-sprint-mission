package com.sprint.mission.discodeit.controller.api.examples;

public class UserExamples {

  public static final String ERROR_400 = """
      {
        "code": "COMMON_001",
        "exceptionType": "VALIDATION_ERROR",
        "message": "request validation failed.",
        "fields": ["username: must not be blank", "email: must be a well-formed email address"]
      }
      """;

  public static final String ERROR_404_USER_003 = """
      {
        "code": "USER_003",
        "exceptionType": "ENTITY_NOT_FOUND",
        "message": "requested user not found."
      }
      """;

  public static final String ERROR_409_USER_001 = """
      {
        "code": "USER_001",
        "exceptionType": "DUPLICATE_RESOURCE",
        "message": "username cannot be duplicated."
      }
      """;

  public static final String ERROR_409_USER_002 = """
      {
        "code": "USER_002",
        "exceptionType": "DUPLICATE_RESOURCE",
        "message": "email cannot be duplicated."
      }
      """;
}