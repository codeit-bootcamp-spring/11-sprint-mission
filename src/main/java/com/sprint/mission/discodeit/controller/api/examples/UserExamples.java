package com.sprint.mission.discodeit.controller.api.examples;

public class UserExamples {

  public static final String CREATE_201 = """
      {
        "success": true,
        "data": {
          "id": "9ea9f98e-0c94-418f-8f14-2335525baf57",
          "nickname": "JohnDoe",
          "username": "johndoe",
          "email": "johndoe@codeit.com",
          "phoneNumber": "123-456-7890",
          "profileId": "82e7fd91-4548-44c4-aca4-5107abf1d52c",
          "status": {
            "lastLoginDate": "2026-03-29T10:56:29.066021Z",
            "isOnline": true
          }
        },
        "error": null
      }
      """;

  public static final String UPDATE_200 = """
      {
        "success": true,
        "data": {
          "id": "9ea9f98e-0c94-418f-8f14-2335525baf57",
          "nickname": "JohnDoe_Updated",
          "username": "johndoe",
          "email": "johndoe@codeit.com",
          "phoneNumber": "123-456-7890",
          "profileId": "c1a2b3d4-0000-41d4-a716-112233445566",
          "status": {
            "lastLoginDate": "2026-03-29T11:00:00.000000Z",
            "isOnline": true
          }
        },
        "error": null
      }
      """;

  public static final String FIND_ALL_200 = """
      {
        "success": true,
        "data": [
          {
            "id": "9ea9f98e-0c94-418f-8f14-2335525baf57",
            "nickname": "JohnDoe",
            "username": "johndoe",
            "email": "johndoe@codeit.com",
            "phoneNumber": "123-456-7890",
            "profileId": "82e7fd91-4548-44c4-aca4-5107abf1d52c",
            "status": {
              "lastLoginDate": "2026-03-29T10:56:29.066021Z",
              "isOnline": true
            }
          }
        ],
        "error": null
      }
      """;

  public static final String UPDATE_STATUS_200 = """
      {
        "success": true,
        "data": {
          "lastLoginDate": "2026-03-29T11:05:00.000000Z",
          "isOnline": true
        },
        "error": null
      }
      """;

  // Error examples
  public static final String ERROR_400_USER_001 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_001",
          "exceptionType": "VALIDATION_ERROR",
          "message": "nickname is required."
        }
      }
      """;

  public static final String ERROR_400_USER_002 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_002",
          "exceptionType": "VALIDATION_ERROR",
          "message": "username is required."
        }
      }
      """;

  public static final String ERROR_409_USER_003 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_003",
          "exceptionType": "DUPLICATE_RESOURCE",
          "message": "username cannot be duplicated."
        }
      }
      """;

  public static final String ERROR_400_USER_004 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_004",
          "exceptionType": "VALIDATION_ERROR",
          "message": "email is required."
        }
      }
      """;

  public static final String ERROR_400_USER_005 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_005",
          "exceptionType": "VALIDATION_ERROR",
          "message": "email format is invalid."
        }
      }
      """;

  public static final String ERROR_409_USER_006 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_006",
          "exceptionType": "DUPLICATE_RESOURCE",
          "message": "email cannot be duplicated."
        }
      }
      """;

  public static final String ERROR_400_USER_007 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_007",
          "exceptionType": "VALIDATION_ERROR",
          "message": "password is required."
        }
      }
      """;

  public static final String ERROR_400_USER_008 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_008",
          "exceptionType": "VALIDATION_ERROR",
          "message": "password length should be at least 8 characters."
        }
      }
      """;

  public static final String ERROR_400_USER_009 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_009",
          "exceptionType": "VALIDATION_ERROR",
          "message": "phone number is required."
        }
      }
      """;

  public static final String ERROR_400_USER_010 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "USER_010",
          "exceptionType": "VALIDATION_ERROR",
          "message": "phone number format is invalid."
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