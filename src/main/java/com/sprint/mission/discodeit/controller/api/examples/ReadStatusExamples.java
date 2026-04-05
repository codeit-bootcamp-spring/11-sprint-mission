package com.sprint.mission.discodeit.controller.api.examples;

public class ReadStatusExamples {

  public static final String CREATE_201 = """
      {
        "success": true,
        "data": {
          "id": "f6a7b8c9-d0e1-2345-f012-3456789abcde",
          "userId": "9ea9f98e-0c94-418f-8f14-2335525baf57",
          "channelId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
          "lastReadAt": "2026-03-29T11:00:00.000000Z"
        },
        "error": null
      }
      """;

  public static final String UPDATE_200 = """
      {
        "success": true,
        "data": {
          "id": "f6a7b8c9-d0e1-2345-f012-3456789abcde",
          "userId": "9ea9f98e-0c94-418f-8f14-2335525baf57",
          "channelId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
          "lastReadAt": "2026-03-29T11:15:00.000000Z"
        },
        "error": null
      }
      """;

  public static final String FIND_ALL_200 = """
      {
        "success": true,
        "data": [
          {
            "id": "f6a7b8c9-d0e1-2345-f012-3456789abcde",
            "userId": "9ea9f98e-0c94-418f-8f14-2335525baf57",
            "channelId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            "lastReadAt": "2026-03-29T11:00:00.000000Z"
          },
          {
            "id": "a7b8c9d0-e1f2-3456-0123-456789abcdef",
            "userId": "9ea9f98e-0c94-418f-8f14-2335525baf57",
            "channelId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
            "lastReadAt": "2026-03-29T10:30:00.000000Z"
          }
        ],
        "error": null
      }
      """;

  // Error examples
  public static final String ERROR_409_READ_STATUS_001 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "READ_STATUS_001",
          "exceptionType": "DUPLICATE_RESOURCE",
          "message": "read status already exists for the same user and channel."
        }
      }
      """;

  public static final String ERROR_404_READ_STATUS_002 = """
      {
        "success": false,
        "data": null,
        "error": {
          "code": "READ_STATUS_002",
          "exceptionType": "ENTITY_NOT_FOUND",
          "message": "requested read status not found."
        }
      }
      """;
}