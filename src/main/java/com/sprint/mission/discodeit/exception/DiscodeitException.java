package com.sprint.mission.discodeit.exception;

public class DiscodeitException extends RuntimeException {

    private final ErrorCode errorCode;

    public DiscodeitException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}