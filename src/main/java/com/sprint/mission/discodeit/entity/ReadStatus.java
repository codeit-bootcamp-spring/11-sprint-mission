package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.util.UUID;

@Getter
public class ReadStatus extends BaseEntity {
    //유저가 채널 별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final UUID channelId;

    // 생성자
    public ReadStatus(UUID userId, UUID channelId) {
        super();
        this.userId = userId;
        this.channelId = channelId;
    }

}