package com.sprint.mission.discodeit.exception;

import lombok.Getter;

public enum ExceptionCode {

    MEMBER_NOT_FOUND(404, "Member Not Found"),
    MEMBER_EXISTS(409, "Member Already Exists"),

    LOGIN_FAILED(401, "Login Failed"),

    FILE_NOT_FOUND(404, "File Not Found"),
    FILE_EMPTY(400, "File Empty"),

    CHANNEL_NOT_FOUND(404, "Channel Not Found"),
    CHANNEL_MODIFY_PRIVATE(400, "Private Channel Cannot Be Modified"),

    MESSAGE_NOT_FOUND(404, "Message Not Found"),
    MESSAGE_CONTENT_EMPTY(400, "Message Content Is Empty"),

    READ_STATUS_EXISTS(409, "Read Status Already Exists"),
    READ_STATUS_NOT_FOUND(404, "Read Status Not Found"),

    EMAIL_EXISTS(409, "Email Already Exists"),
    USER_NOT_FOUND(404, "User Not Found"),
    USER_STATUS_NOT_FOUND(404, "User Status Not Found"),
    PASSWORD_NOT_MATCH(401, "Password Does Not Match"),

    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int code, String message) {
        this.status = code;
        this.message = message;
    }
}
