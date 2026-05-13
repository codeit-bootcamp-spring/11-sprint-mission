package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message,
        Map<String, Object> details,
        String exceptionType,
        int status
) {

    public static ErrorResponse of(HttpStatus status, String code, String message, Map<String, Object> details, String exceptionType) {
        return new ErrorResponse(
                Instant.now(),
                code,
                message,
                details,
                exceptionType,
                status.value()
        );
    }

    public static ErrorResponse of(HttpStatus status, String code, String message, String exceptionType) {
        return of(status, code, message, Map.of(), exceptionType);
    }
}
