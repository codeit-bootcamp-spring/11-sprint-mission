package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.dto.error.ErrorResponse;
import com.sprint.mission.discodeit.exception.auth.InvalidAuthRequestException;
import com.sprint.mission.discodeit.exception.auth.LoginFailedException;
import com.sprint.mission.discodeit.exception.binaryContent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binaryContent.InvalidBinaryContentRequestException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelOperationException;
import com.sprint.mission.discodeit.exception.channel.InvalidChannelRequestException;
import com.sprint.mission.discodeit.exception.channel.NotChannelAdminException;
import com.sprint.mission.discodeit.exception.message.InvalidMessageRequestException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.readStatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readStatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.InvalidUserRequestException;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userStatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.userStatus.UserStatusNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildErrorResponse(String code, String message, HttpStatus status) {
        ErrorResponse response = new ErrorResponse(code, message, LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }

    //Auth Exceptions
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponse> handleLoginFailedException(LoginFailedException e) {
        return buildErrorResponse("AUTH_001", e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidAuthRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAuthRequestException(InvalidAuthRequestException e) {
        return buildErrorResponse("AUTH_002", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //User Exceptions
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        return buildErrorResponse("USER_001", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return buildErrorResponse("USER_002", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidUserRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserRequestException(InvalidUserRequestException e) {
        return buildErrorResponse("USER_003", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //Channel Exceptions
    @ExceptionHandler(ChannelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChannelNotFoundException(ChannelNotFoundException e) {
        return buildErrorResponse("CHANNEL_001", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidChannelRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidChannelRequestException(InvalidChannelRequestException e) {
        return buildErrorResponse("CHANNEL_002", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotChannelAdminException.class)
    public ResponseEntity<ErrorResponse> handleNotChannelAdminException(NotChannelAdminException e) {
        return buildErrorResponse("CHANNEL_003", e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ChannelOperationException.class)
    public ResponseEntity<ErrorResponse> handleChannelOperationException(ChannelOperationException e) {
        return buildErrorResponse("CHANNEL_004", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //Message Exceptions
    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotFoundException(MessageNotFoundException e) {
        return buildErrorResponse("MSG_001", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidMessageRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMessageRequestException(InvalidMessageRequestException e) {
        return buildErrorResponse("MSG_002", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //Binary Content Exceptions
    @ExceptionHandler(BinaryContentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBinaryContentNotFoundException(BinaryContentNotFoundException e) {
        return buildErrorResponse("BIN_001", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidBinaryContentRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBinaryContentRequestException(InvalidBinaryContentRequestException e) {
        return buildErrorResponse("BIN_002", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //Read Status Exceptions
    @ExceptionHandler(ReadStatusNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReadStatusNotFoundException(ReadStatusNotFoundException e) {
        return buildErrorResponse("READ_001", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReadStatusAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleReadStatusAlreadyExistsException(ReadStatusAlreadyExistsException e) {
        return buildErrorResponse("READ_002", e.getMessage(), HttpStatus.CONFLICT);
    }

    //User Status Exceptions
    @ExceptionHandler(UserStatusNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserStatusNotFoundException(UserStatusNotFoundException e) {
        return buildErrorResponse("USTR_001", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserStatusAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserStatusAlreadyExistsException(UserStatusAlreadyExistsException e) {
        return buildErrorResponse("USTR_002", e.getMessage(), HttpStatus.CONFLICT);
    }

    //General Exception (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        e.printStackTrace();
        return buildErrorResponse("SERVER_ERROR", "서버 내부에서 알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
