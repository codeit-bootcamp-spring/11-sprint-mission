package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String exceptionType,
        Map<String, Object> details
) {

    public static ErrorResponse from(DiscodeitException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        return new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                exception.getClass().getSimpleName(),
                exception.getDetails()
        );
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            String exceptionType,
            Map<String, Object> details
    ) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                exceptionType,
                details == null ? Map.of() : details
        );
    }
}