package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "User email already exists"),
    USER_NAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Username already exists"),
    USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "User status not found"),
    USER_STATUS_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "User status already exists"),

    // Auth
    AUTH_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "Invalid password"),

    // Channel
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "Channel not found"),
    CHANNEL_UPDATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Private channel cannot be updated"),
    CHANNEL_PARTICIPANT_DUPLICATED(HttpStatus.BAD_REQUEST, "Channel participant duplicated"),
    CHANNEL_INVALID_PARTICIPANT_ID(HttpStatus.BAD_REQUEST, "Invalid channel participant id"),

    // Message
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Message not found"),

    // ReadStatus
    READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "Read status not found"),
    READ_STATUS_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Read status already exists"),

    // BinaryContent
    BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Binary content not found"),
    DIRECTORY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
    FILE_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
