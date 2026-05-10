package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    Instant timestamp,
    String code,
    String message,
    Map<String, Object> details,
    String exceptionType,
    int status
) {

    public static ErrorResponse from(DiscodeitException e, int status) {
        return new ErrorResponse(
            e.getTimestamp(),
            e.getErrorCode().name(),
            e.getErrorCode().getMessage(),
            e.getDetails(),
            e.getClass().getSimpleName(),
            status
        );
    }

    public static ErrorResponse from(Exception e, int status) {
        return new ErrorResponse(
            Instant.now(),
            "INTERNAL_ERROR",
            e.getMessage(),
            Map.of(),
            e.getClass().getSimpleName(),
            status
        );
    }
}