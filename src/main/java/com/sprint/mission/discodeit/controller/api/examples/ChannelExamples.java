package com.sprint.mission.discodeit.controller.api.examples;

public class ChannelExamples {

    public static final String CREATE_PUBLIC_201 = """
            {
              "success": true,
              "data": {
                "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                "name": "general",
                "description": "General discussion channel",
                "isPrivate": false,
                "lastMessageAt": null,
                "participants": []
              },
              "error": null
            }
            """;

    public static final String CREATE_PRIVATE_201 = """
            {
              "success": true,
              "data": {
                "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                "name": null,
                "description": null,
                "isPrivate": true,
                "lastMessageAt": null,
                "participants": [
                  "9ea9f98e-0c94-418f-8f14-2335525baf57",
                  "b2c3d4e5-f6a7-8901-bcde-f12345678901"
                ]
              },
              "error": null
            }
            """;

    public static final String UPDATE_200 = """
            {
              "success": true,
              "data": {
                "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                "name": "general-updated",
                "description": "Updated description",
                "isPrivate": false,
                "lastMessageAt": "2026-03-29T10:50:00.000000Z",
                "participants": []
              },
              "error": null
            }
            """;

    public static final String FIND_ALL_200 = """
            {
              "success": true,
              "data": [
                {
                  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "name": "general",
                  "description": "General discussion channel",
                  "isPrivate": false,
                  "lastMessageAt": "2026-03-29T10:50:00.000000Z",
                  "participants": []
                },
                {
                  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                  "name": null,
                  "description": null,
                  "isPrivate": true,
                  "lastMessageAt": "2026-03-29T11:00:00.000000Z",
                  "participants": [
                    "9ea9f98e-0c94-418f-8f14-2335525baf57",
                    "c3d4e5f6-a7b8-9012-cdef-123456789012"
                  ]
                }
              ],
              "error": null
            }
            """;

    // Error examples
    public static final String ERROR_400_CHANNEL_001 = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "CHANNEL_001",
                "exceptionType": "VALIDATION_ERROR",
                "message": "name is required."
              }
            }
            """;

    public static final String ERROR_409_CHANNEL_002 = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "CHANNEL_002",
                "exceptionType": "DUPLICATE_RESOURCE",
                "message": "name cannot be duplicated."
              }
            }
            """;

    public static final String ERROR_400_CHANNEL_003 = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "CHANNEL_003",
                "exceptionType": "VALIDATION_ERROR",
                "message": "participants is required."
              }
            }
            """;

    public static final String ERROR_400_CHANNEL_004 = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "CHANNEL_004",
                "exceptionType": "VALIDATION_ERROR",
                "message": "no valid participants found."
              }
            }
            """;

    public static final String ERROR_404_CHANNEL_005 = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "CHANNEL_005",
                "exceptionType": "ENTITY_NOT_FOUND",
                "message": "requested channel not found."
              }
            }
            """;

    public static final String ERROR_422_CHANNEL_006 = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "CHANNEL_006",
                "exceptionType": "INVALID_OPERATION",
                "message": "private channel cannot be updated."
              }
            }
            """;
}