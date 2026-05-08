package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BinaryContentErrorCode implements ErrorCode {
    BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "BIN-001", "존재하지 않는 바이너리 콘텐츠입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
