package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.dto.common.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.COMMON_UNEXPECTED_ERROR;

@ResponseBody
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<RestResponse<Void>> handleDiscodeitException(ApiException e) {
        return ResponseEntity
                .status(e.getError().getHttpStatus())
                .body(RestResponse.error(ErrorResponse.from(e.getError())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RestResponse.error(ErrorResponse.from(COMMON_UNEXPECTED_ERROR)));
    }
}
