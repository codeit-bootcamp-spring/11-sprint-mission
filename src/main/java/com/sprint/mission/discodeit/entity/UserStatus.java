package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Getter
public class UserStatus extends BaseEntity {
//사용자 별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델


    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID userId;

    // 생성자
    public UserStatus(UUID userId) {
        super();
        this.userId = userId;
    }

    public boolean isOnline() {
        Instant now = Instant.now();// 현재시간
        Instant fiveMinutesAgo = now.minus(5, ChronoUnit.MINUTES); // 5분전 시간

        // 마지막 업데이트 시간이 '5분전시간' 이후 인가? yes -> 온라인상태
        return getUpdatedAt().isAfter(fiveMinutesAgo);
    }
}