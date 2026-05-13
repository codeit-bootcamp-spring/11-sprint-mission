package com.sprint.mission.discodeit.controller.api.examples;

public class ChannelExamples {

  public static final String ERROR_400_VALIDATION = """
      {
        "code": "COMMON_001",
        "exceptionType": "VALIDATION_ERROR",
        "message": "request validation failed.",
        "fields": ["name: must not be blank"]
      }
      """;

  public static final String ERROR_400_CHANNEL_002 = """
      {
        "code": "CHANNEL_002",
        "exceptionType": "VALIDATION_ERROR",
        "message": "no valid participants found."
      }
      """;

  public static final String ERROR_404_CHANNEL_003 = """
      {
        "code": "CHANNEL_003",
        "exceptionType": "ENTITY_NOT_FOUND",
        "message": "requested channel not found."
      }
      """;

  public static final String ERROR_409_CHANNEL_001 = """
      {
        "code": "CHANNEL_001",
        "exceptionType": "DUPLICATE_RESOURCE",
        "message": "name cannot be duplicated."
      }
      """;

  public static final String ERROR_422_CHANNEL_004 = """
      {
        "code": "CHANNEL_004",
        "exceptionType": "INVALID_OPERATION",
        "message": "private channel cannot be updated."
      }
      """;
}