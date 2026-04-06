package com.sprint.mission.discodeit.controller.api.examples;

public class AuthExamples {

  public static final String LOGIN_200 = """
      {
        "success": true,
        "data": {
          "id": "9ea9f98e-0c94-418f-8f14-2335525baf57",
          "nickname": "JohnDoe",
          "username": "johndoe",
          "email": "johndoe@codeit.com",
          "phoneNumber": "123-456-7890",
          "profileId": null,
          "status": {
            "lastLoginDate": "2026-03-29T10:56:29.066021Z",
            "isOnline": true
          }
        },
        "error": null
      }
      """;

  public static final String ERROR_400_AUTH_001 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "AUTH_001",
          "exceptionType": "VALIDATION_ERROR",
          "message": "username is required."
        }
      }
      """;

  public static final String ERROR_400_AUTH_002 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "AUTH_002",
          "exceptionType": "VALIDATION_ERROR",
          "message": "password is required."
        }
      }
      """;

  public static final String ERROR_401_AUTH_003 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "AUTH_003",
          "exceptionType": "INVALID_CREDENTIALS",
          "message": "password is not matched."
        }
      }
      """;

  public static final String ERROR_404_USER_011 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_011",
          "exceptionType": "ENTITY_NOT_FOUND",
          "message": "requested user not found."
        }
      }
      """;
}