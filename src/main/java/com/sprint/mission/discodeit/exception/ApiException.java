package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final ERROR error;

    public enum ERROR {
        // Auth
        AUTH_USERNAME_REQUIRED("AUTH_001", "VALIDATION_ERROR", "username is required.", HttpStatus.BAD_REQUEST),
        AUTH_PASSWORD_REQUIRED("AUTH_002", "VALIDATION_ERROR", "password is required.", HttpStatus.BAD_REQUEST),
        AUTH_INVALID_CREDENTIALS("AUTH_003", "INVALID_CREDENTIALS", "password is not matched.", HttpStatus.UNAUTHORIZED),

        // User
        USER_NICKNAME_REQUIRED("USER_001", "VALIDATION_ERROR", "nickname is required.", HttpStatus.BAD_REQUEST),
        USER_USERNAME_REQUIRED("USER_002", "VALIDATION_ERROR", "username is required.", HttpStatus.BAD_REQUEST),
        USER_USERNAME_DUPLICATED("USER_003", "DUPLICATE_RESOURCE", "username cannot be duplicated.", HttpStatus.CONFLICT),
        USER_EMAIL_REQUIRED("USER_004", "VALIDATION_ERROR", "email is required.", HttpStatus.BAD_REQUEST),
        USER_INVALID_EMAIL_FORMAT("USER_005", "VALIDATION_ERROR", "email format is invalid.", HttpStatus.BAD_REQUEST),
        USER_EMAIL_DUPLICATED("USER_006", "DUPLICATE_RESOURCE", "email cannot be duplicated.", HttpStatus.CONFLICT),
        USER_PASSWORD_REQUIRED("USER_007", "VALIDATION_ERROR", "password is required.", HttpStatus.BAD_REQUEST),
        USER_INVALID_PASSWORD_LENGTH("USER_008", "VALIDATION_ERROR", "password length should be at least 8 characters.", HttpStatus.BAD_REQUEST),
        USER_PHONE_NUMBER_REQUIRED("USER_009", "VALIDATION_ERROR", "phone number is required.", HttpStatus.BAD_REQUEST),
        USER_INVALID_PHONE_NUMBER_FORMAT("USER_010", "VALIDATION_ERROR", "phone number format is invalid.", HttpStatus.BAD_REQUEST),
        USER_NOT_FOUND("USER_011", "ENTITY_NOT_FOUND", "requested user not found.", HttpStatus.NOT_FOUND),

        // Channel
        CHANNEL_NAME_REQUIRED("CHANNEL_001", "VALIDATION_ERROR", "name is required.", HttpStatus.BAD_REQUEST),
        CHANNEL_NAME_DUPLICATED("CHANNEL_002", "DUPLICATE_RESOURCE", "name cannot be duplicated.", HttpStatus.CONFLICT),
        CHANNEL_PARTICIPANTS_REQUIRED("CHANNEL_003", "VALIDATION_ERROR", "participants is required.", HttpStatus.BAD_REQUEST),
        CHANNEL_NO_VALID_PARTICIPANTS("CHANNEL_004", "VALIDATION_ERROR", "no valid participants found.", HttpStatus.BAD_REQUEST),
        CHANNEL_NOT_FOUND("CHANNEL_005", "ENTITY_NOT_FOUND", "requested channel not found.", HttpStatus.NOT_FOUND),
        CHANNEL_PRIVATE_UPDATE_FORBIDDEN("CHANNEL_006", "INVALID_OPERATION", "private channel cannot be updated.", HttpStatus.UNPROCESSABLE_ENTITY),

        // Message
        MESSAGE_CONTENT_REQUIRED("MESSAGE_001", "VALIDATION_ERROR", "content is required.", HttpStatus.BAD_REQUEST),
        MESSAGE_CHANNEL_ACCESS_REQUIRED("MESSAGE_002", "INVALID_OPERATION", "sender cannot send message without channel participation.", HttpStatus.UNPROCESSABLE_ENTITY),
        MESSAGE_NOT_FOUND("MESSAGE_003", "ENTITY_NOT_FOUND", "requested message not found.", HttpStatus.NOT_FOUND),

        // ReadStatus
        READ_STATUS_DUPLICATED("READ_STATUS_001", "DUPLICATE_RESOURCE", "read status already exists for the same user and channel.", HttpStatus.CONFLICT),
        READ_STATUS_NOT_FOUND("READ_STATUS_002", "ENTITY_NOT_FOUND", "requested read status not found.", HttpStatus.NOT_FOUND),

        // UserStatus
        USER_STATUS_DUPLICATED("USER_STATUS_001", "DUPLICATE_RESOURCE", "user status already exists for the same user.", HttpStatus.CONFLICT),
        USER_STATUS_NOT_FOUND("USER_STATUS_002", "ENTITY_NOT_FOUND", "requested user status not found.", HttpStatus.NOT_FOUND),

        // BinaryContent
        BINARY_CONTENT_NOT_FOUND("BINARY_CONTENT_001", "ENTITY_NOT_FOUND", "requested binary content not found.", HttpStatus.NOT_FOUND),

        // Common
        COMMON_UNEXPECTED_ERROR("COMMON_001", "INTERNAL_SERVER_ERROR", "unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);

        private final String code;
        private final String exceptionType;
        private final String message;
        private final HttpStatus httpStatus;

        ERROR(String code, String exceptionType, String message, HttpStatus httpStatus) {
            this.code = code;
            this.exceptionType = exceptionType;
            this.message = message;
            this.httpStatus = httpStatus;
        }

        public String getCode() { return this.code; }
        public String getExceptionType() { return this.exceptionType; }
        public String getMessage() { return this.message; }
        public HttpStatus getHttpStatus() { return this.httpStatus; }
    }

    public ApiException(ERROR error) {
        super(error.getMessage());
        this.error = error;
    }

    public ERROR getError() {
        return this.error;
    }
}
