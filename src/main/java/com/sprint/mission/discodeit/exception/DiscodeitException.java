package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class DiscodeitException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public DiscodeitException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = Map.of();
    }

    public DiscodeitException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details == null ? Map.of() : details;
    }

    public DiscodeitException(ErrorCode errorCode, Map<String, Object> details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = details == null ? Map.of() : details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}