package com.sprint.mission.discodeit.controller.api.examples;

public class ReadStatusExamples {

  public static final String ERROR_400 = """
      {
        "code": "COMMON_001",
        "exceptionType": "VALIDATION_ERROR",
        "message": "request validation failed.",
        "fields": ["userId: must not be null", "channelId: must not be null"]
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

  public static final String ERROR_404_READ_STATUS_002 = """
      {
        "code": "READ_STATUS_002",
        "exceptionType": "ENTITY_NOT_FOUND",
        "message": "requested read status not found."
      }
      """;

  public static final String ERROR_409_READ_STATUS_001 = """
      {
        "code": "READ_STATUS_001",
        "exceptionType": "DUPLICATE_RESOURCE",
        "message": "read status already exists for the same user and channel."
      }
      """;
}