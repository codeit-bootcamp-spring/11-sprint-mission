package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserStatusErrorCode implements ErrorCode {

    USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "UST-001", "사용자 상태 정보를 찾을 수 없습니다."),
    USER_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "UST-002", "해당 사용자의 상태 정보가 이미 존재합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
