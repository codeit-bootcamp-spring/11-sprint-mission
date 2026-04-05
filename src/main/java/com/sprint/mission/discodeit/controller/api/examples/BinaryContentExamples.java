package com.sprint.mission.discodeit.controller.api.examples;

public class BinaryContentExamples {

    public static final String FIND_BY_ID_200 = """
            {
              "success": true,
              "data": {
                "bytes": "<byte array>",
                "fileName": "profile.png",
                "contentType": "image/png",
                "size": 204800
              },
              "error": null
            }
            """;

    public static final String FIND_ALL_BY_IDS_200 = """
            {
              "success": true,
              "data": [
                {
                  "bytes": "<byte array>",
                  "fileName": "attachment1.png",
                  "contentType": "image/png",
                  "size": 204800
                },
                {
                  "bytes": "<byte array>",
                  "fileName": "document.pdf",
                  "contentType": "application/pdf",
                  "size": 512000
                }
              ],
              "error": null
            }
            """;

    // Error examples
    public static final String ERROR_404_BINARY_CONTENT_001 = """
            {
              "success": false,
              "data": null,
              "error": {
                "code": "BINARY_CONTENT_001",
                "exceptionType": "ENTITY_NOT_FOUND",
                "message": "requested binary content not found."
              }
            }
            """;
}