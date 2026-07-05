package com.sprint.mission.discodeit.controller.api.examples;

public class AuthExamples {

  public static final String ERROR_401_AUTH_002 = """
      {
        "code": "AUTH_001",
        "exceptionType": "InvalidUserDetailsException",
        "message": "user details is invalid."
      }
      """;

  public static final String ERROR_404_USER_001 = """
      {
        "code": "USER_001",
        "exceptionType": "UserNotFoundException",
        "message": "requested user not found."
      }
      """;

  public static final String ERROR_401_AUTH_006 = """
      {
        "code": "AUTH_006",
        "exceptionType": "InvalidRefreshTokenException",
        "message": "refresh token is invalid or expired."
      }
      """;
}