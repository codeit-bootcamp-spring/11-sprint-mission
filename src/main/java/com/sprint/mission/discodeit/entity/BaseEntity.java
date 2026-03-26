package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    // 상속으로 해야 시스템 기능이나 새로운 객체 생성시 유지보수 편리함
    // id, createdAt : 생성자에서 초기화 >> 객체 new 자동값 세팅, 외부 setter X
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt; // 얘는 계속 정보가 바뀌니까 final 선언 x

    private static final DateTimeFormatter FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public BaseEntity() {
        // 유저 이름이 들어오면, 식별id, 생성시간, 업데이트 시간
        this.id = UUID.randomUUID(); // ?? 불릴때마다 생성이 되는지 ??
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt; // 처음에는 이렇게 설정해줌
    }

    public String getUpdatedAtText() {
        return FORMATTER.format(updatedAt);
    }

    public String getCreatedAtText() {
        return FORMATTER.format(createdAt);
    }

    protected void touch() {
        // 매번 쓰는거 방지 + 유지보수 측면
        this.updatedAt = Instant.now();
    }

}
