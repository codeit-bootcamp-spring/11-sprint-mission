package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessageErrorCode implements ErrorCode {

    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MSG-001", "존재하지 않는 메시지입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
