package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // File IO Error
    FILE_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 읽기/쓰기 중 오류가 발생했습니다."),
    FILE_DATA_CORRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 데이터가 손상되었거나 형식이 맞지 않습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    USER_EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "해당 이메일은 이미 사용중입니다."),
    USER_USERNAME_DUPLICATE(HttpStatus.BAD_REQUEST, "해당 유저명은 이미 사용중입니다."),

    // UserStatus
    USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저의 상태 정보를 찾을 수 없습니다."),

    // Channel
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 채널을 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    ALREADY_JOIN_CHANNEL(HttpStatus.BAD_REQUEST, "이미 가입된 채널입니다."),
    MASTER_NOT_LEAVE(HttpStatus.BAD_REQUEST, "방장은 채널을 탈퇴할 수 없습니다. 채널을 삭제하거나, 방장을 위임해주세요."),
    PRIVATE_NOT_UPDATE(HttpStatus.BAD_REQUEST, "Private 채널은 수정할 수 없습니다."),

    // BinaryContent
    CONTENT_NOT_EXIST(HttpStatus.NOT_FOUND, "해당 파일은 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
