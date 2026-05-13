package com.sprint.mission.discodeit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("비즈니스 예외 발생: errorCode={}, details={}",
                errorCode.name(),
                e.getDetails()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.from(e));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        Map<String, Object> details = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (oldValue, newValue) -> oldValue
                ));

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.VALIDATION_FAILED,
                e.getClass().getSimpleName(),
                details
        );

        log.warn("요청 값 검증 실패: details={}", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_REQUEST,
                e.getClass().getSimpleName(),
                Map.of("reason", "요청 JSON 형식이 올바르지 않습니다.")
        );

        log.warn("요청 본문 파싱 실패: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(
            MissingServletRequestPartException e
    ) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_REQUEST,
                e.getClass().getSimpleName(),
                Map.of("missingPart", e.getRequestPartName())
        );

        log.warn("multipart 요청 파트 누락: part={}", e.getRequestPartName());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e
    ) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_REQUEST,
                e.getClass().getSimpleName(),
                Map.of("reason", "지원하지 않는 Content-Type 입니다.")
        );

        log.warn("지원하지 않는 Content-Type 요청: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INVALID_REQUEST,
                e.getClass().getSimpleName(),
                Map.of("reason", e.getMessage())
        );

        log.warn("잘못된 요청 예외 발생: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR,
                e.getClass().getSimpleName(),
                Map.of("reason", "예상하지 못한 서버 오류가 발생했습니다.")
        );

        log.error("예상하지 못한 서버 오류", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}