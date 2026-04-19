package com.sprint.mission.discodeit.controller.api.examples;

public class MessageExamples {

  public static final String FIND_ALL_200 = """
      {
        "content": [
          {
            "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            "createdAt": "2026-04-19T11:07:33.410Z",
            "updatedAt": "2026-04-19T11:07:33.410Z",
            "content": "string",
            "channelId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            "author": {
              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "username": "string",
              "email": "string",
              "profile": {
                "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                "fileName": "string",
                "size": 0,
                "contentType": "string"
              },
              "online": true
            },
            "attachments": [
              {
                "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                "fileName": "string",
                "size": 0,
                "contentType": "string"
              }
            ]
          }
        ],
        "nextCursor": "2026-04-19T11:07:33.410Z",
        "size": 50,
        "hasNext": true,
        "totalElements": null
      }
      """;

  public static final String ERROR_400 = """
      {
        "code": "COMMON_001",
        "exceptionType": "VALIDATION_ERROR",
        "message": "request validation failed.",
        "fields": ["content: must not be blank"]
      }
      """;

  public static final String ERROR_404_USER_003 = """
      {
        "code": "USER_003",
        "exceptionType": "ENTITY_NOT_FOUND",
        "message": "requested user not found."
      }
      """;

  public static final String ERROR_404_CHANNEL_003 = """
      {
        "code": "CHANNEL_003",
        "exceptionType": "ENTITY_NOT_FOUND",
        "message": "requested channel not found."
      }
      """;

  public static final String ERROR_404_MESSAGE_002 = """
      {
        "code": "MESSAGE_002",
        "exceptionType": "ENTITY_NOT_FOUND",
        "message": "requested message not found."
      }
      """;

  public static final String ERROR_422_MESSAGE_001 = """
      {
        "code": "MESSAGE_001",
        "exceptionType": "INVALID_OPERATION",
        "message": "sender cannot send message without channel participation."
      }
      """;
}