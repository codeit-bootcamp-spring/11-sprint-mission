package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChannelErrorCode implements ErrorCode {

    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CHN-001", "존재하지 않는 채널입니다."),
    PRIVATE_CHANNEL_NOT_UPDATABLE(HttpStatus.BAD_REQUEST, "CHN-002", "프라이빗 채널은 수정할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
