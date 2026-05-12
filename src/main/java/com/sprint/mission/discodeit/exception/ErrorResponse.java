package com.sprint.mission.discodeit.exception;

import java.util.Map;

public record ErrorResponse(
        int status,
        String exceptionType,
        String message,
        Map<String, Object> details
) {
    public static ErrorResponse from(DiscodeitException e) {
        return new ErrorResponse(
                e.getErrorCode().getStatus().value(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                e.getDetails()
        );
    }
}
