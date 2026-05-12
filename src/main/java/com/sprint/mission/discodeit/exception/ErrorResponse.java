package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ErrorResponse {

    private final Instant timestamp;
    private final String code;
    private final String message;
    private final Map<String, Object> details;
    private final String exceptionType;
    private final int status;

    public ErrorResponse(Exception e) {
        this.timestamp = Instant.now();
        this.code = GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage();
        this.details = Collections.emptyMap();
        this.exceptionType = e.getClass().getSimpleName();
        this.status = GlobalErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value();
    }

    public ErrorResponse(Exception e, String code, String message, HttpStatus status) {
        this.timestamp = Instant.now();
        this.code = code;
        this.message = message;
        this.details = new HashMap<>(); // 기본 에러는 상세 정보 없음
        this.exceptionType = e.getClass().getSimpleName();
        this.status = status.value();
    }

    public ErrorResponse(DiscodeitException e) {
        this.timestamp = e.getTimestamp();
        this.code = e.getErrorCode().getCode();
        this.message = e.getErrorCode().getMessage();
        this.details = e.getDetails();
        this.exceptionType = e.getClass().getSimpleName();
        this.status = e.getErrorCode().getHttpStatus().value();
    }

    public ErrorResponse(MethodArgumentNotValidException e) {
        this.timestamp = Instant.now();
        this.code = GlobalErrorCode.VALIDATION_FAILED.getCode();
        this.message = GlobalErrorCode.VALIDATION_FAILED.getMessage();
        Map<String, Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage()));
        this.details = details;
        this.exceptionType = e.getClass().getSimpleName();
        this.status = GlobalErrorCode.VALIDATION_FAILED.getHttpStatus().value();
    }

    // TODO: 정적 팩토리 메서드
    // - 생성자는 생성만 하고
    // - 팩토리 메서드로 저 details 같은걸 돌리자
}
