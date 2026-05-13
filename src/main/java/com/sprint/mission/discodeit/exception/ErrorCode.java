package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    USER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
    USER_USERNAME_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 유저명입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다"),

    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다"),
    CHANNEL_NOT_UPDATABLE(HttpStatus.BAD_REQUEST, "프라이빗 채널은 수정할 수 없습니다"),

    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다"),

    READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "읽기 상태를 찾을 수 없습니다"),

    USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 상태를 찾을 수 없습니다"),
    USER_STATUS_DUPLICATE(HttpStatus.CONFLICT, "이미 사용자 상태가 존재합니다"),

    BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "파일 정보를 찾을 수 없습니다"),
    FILE_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
