package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 값 검증에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),

    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
    PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PRIVATE 채널은 수정할 수 없습니다."),

    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),

    READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "읽음 상태를 찾을 수 없습니다."),
    READ_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 사용자와 채널의 읽음 상태가 존재합니다."),

    USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 상태를 찾을 수 없습니다."),
    USER_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 사용자의 상태가 존재합니다."),

    BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "파일 정보를 찾을 수 없습니다."),
    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 처리 중 오류가 발생했습니다.");

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
