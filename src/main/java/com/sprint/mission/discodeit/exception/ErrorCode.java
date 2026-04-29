package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "Channel not found"),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Message not found"),
    BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Binary content not found"),
    USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "User status not found"),
    READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "Read status not found"),

    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "Duplicate email"),
    DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "Duplicate name"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "Invalid password"),
    PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Private channel cannot be updated"),
    DUPLICATE_PARTICIPANT(HttpStatus.BAD_REQUEST, "Duplicate participant"),
    READ_STATUS_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Read status already exists"),
    USER_STATUS_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "User status already exists"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
    FILE_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
    DIRECTORY_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
