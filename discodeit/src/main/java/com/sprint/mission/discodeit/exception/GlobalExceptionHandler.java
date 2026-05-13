package com.sprint.mission.discodeit.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException exception) {
        HttpStatus status = exception.getErrorCode().getStatus();
        ErrorResponse error = ErrorResponse.of(
                status,
                exception.getErrorCode().getCode(),
                exception.getMessage(),
                exception.getDetails(),
                exception.getClass().getSimpleName()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, Object> details = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않은 값입니다.",
                        (existing, replacement) -> existing
                ));
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_REQUEST.getCode(),
                "입력값 유효성 검증에 실패했어요.",
                details,
                exception.getClass().getSimpleName()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = ErrorCode.INVALID_REQUEST.getMessage();
        }
        return respond(ErrorCode.INVALID_REQUEST.getStatus(), ErrorCode.INVALID_REQUEST.getCode(), message, exception.getClass().getSimpleName());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("처리되지 않은 예외 발생: {} {}", request.getMethod(), request.getRequestURI(), exception);
        return respond(
                ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                exception.getClass().getSimpleName()
        );
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String code, String message, String exceptionType) {
        ErrorResponse error = ErrorResponse.of(status, code, message, exceptionType);
        return ResponseEntity.status(status).body(error);
    }
}
