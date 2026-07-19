package com.sprint.mission.discodeit.controller.api.examples;

public class NotificationExamples {

  public static final String FIND_ALL_200 = """
      [
        {
          "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "receiverId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "title": "string (#string)",
          "content": "string",
          "createdAt": "2026-04-19T11:07:33.410Z"
        }
      ]
      """;

  public static final String ERROR_401_AUTH_003 = """
      {
        "code": "AUTH_003",
        "exceptionType": "UnauthorizedException",
        "message": "authentication is required."
      }
      """;

  public static final String ERROR_403_AUTH_004 = """
      {
        "code": "AUTH_004",
        "exceptionType": "ForbiddenException",
        "message": "access is denied."
      }
      """;

  public static final String ERROR_404_NOTIFICATION_001 = """
      {
        "code": "NOTIFICATION_001",
        "exceptionType": "NotificationNotFoundException",
        "message": "requested notification not found."
      }
      """;
}