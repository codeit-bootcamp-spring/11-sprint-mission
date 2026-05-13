package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.dto.ErrorResponse;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.InvalidPasswordException;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.DuplicateUserStatusException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("User not found: {}", e.getDetails());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from(e, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        log.warn("Duplicate user: {}", e.getDetails());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse.from(e, HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException e) {
        log.warn("Invalid password attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.from(e, HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(ChannelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChannelNotFoundException(ChannelNotFoundException e) {
        log.warn("Channel not found: {}", e.getDetails());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from(e, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(PrivateChannelUpdateException.class)
    public ResponseEntity<ErrorResponse> handlePrivateChannelUpdateException(PrivateChannelUpdateException e) {
        log.warn("Private channel update attempt: {}", e.getDetails());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.from(e, HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotFoundException(MessageNotFoundException e) {
        log.warn("Message not found: {}", e.getDetails());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from(e, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(BinaryContentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBinaryContentNotFoundException(BinaryContentNotFoundException e) {
        log.warn("BinaryContent not found: {}", e.getDetails());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from(e, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(ReadStatusNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReadStatusNotFoundException(ReadStatusNotFoundException e) {
        log.warn("ReadStatus not found: {}", e.getDetails());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from(e, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(UserStatusNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserStatusNotFoundException(UserStatusNotFoundException e) {
        log.warn("UserStatus not found: {}", e.getDetails());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.from(e, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(DuplicateUserStatusException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUserStatusException(DuplicateUserStatusException e) {
        log.warn("Duplicate UserStatus: {}", e.getDetails());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse.from(e, HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, Object> details = e.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> (Object) fe.getDefaultMessage(),
                (a, b) -> a
            ));
        log.warn("Validation failed: {}", details);
        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            ErrorCode.VALIDATION_FAILED.name(),
            ErrorCode.VALIDATION_FAILED.getMessage(),
            details,
            e.getClass().getSimpleName(),
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
        log.warn("Discodeit exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.from(e, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.from(e, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}