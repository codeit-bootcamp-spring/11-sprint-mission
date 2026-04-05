package com.sprint.mission.discodeit.controller.api.examples;

public class MessageExamples {

    public static final String CREATE_201 = """
            {
              "success": true,
              "data": {
                "id": "d4e5f6a7-b8c9-0123-def0-123456789abc",
                "createdAt": "2026-03-29T11:00:00.000000Z",
                "updatedAt": "2026-03-29T11:00:00.000000Z",
                "content": "Hello, world!",
                "senderId": "9ea9f98e-0c94-418f-8f14-2335525baf57",
                "channelId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                "attachmentIds": [
                  "e5f6a7b8-c9d0-1234-ef01-23456789abcd"
                ]
              },
              "error": null
            }
            """;

    public static final String UPDATE_200 = """
            {
              "success": true,
              "data": {
                "id": "d4e5f6a7-b8c9-0123-def0-123456789abc",
                "createdAt": "2026-03-29T11:00:00.000000Z",
                "updatedAt": "2026-03-29T11:10:00.000000Z",
                "content": "Updated message content",
                "senderId": "9ea9f98e-0c94-418f-8f14-2335525baf57",
                "channelId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                "attachmentIds": []
              },
              "error": null
            }
            """;

    public static final String FIND_ALL_200 = """
            {
              "success": true,
              "data": [
                {
                  "id": "d4e5f6a7-b8c9-0123-def0-123456789abc",
                  "createdAt": "2026-03-29T11:00:00.000000Z",
                  "updatedAt": "2026-03-29T11:00:00.000000Z",
                  "content": "Hello, world!",
                  "senderId": "9ea9f98e-0c94-418f-8f14-2335525baf57",
                  "channelId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "attachmentIds": []
                }
              ],
              "error": null
            }
            """;

    // Error examples
    public static final String ERROR_400_MESSAGE_001 = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "MESSAGE_001",
                "exceptionType": "VALIDATION_ERROR",
                "message": "content is required."
              }
            }
            """;

    public static final String ERROR_422_MESSAGE_002 = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "MESSAGE_002",
                "exceptionType": "INVALID_OPERATION",
                "message": "sender cannot send message without channel participation."
              }
            }
            """;

    public static final String ERROR_404_MESSAGE_003 = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "MESSAGE_003",
                "exceptionType": "ENTITY_NOT_FOUND",
                "message": "requested message not found."
              }
            }
            """;
}