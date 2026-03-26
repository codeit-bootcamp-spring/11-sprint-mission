package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;


@Getter
@Builder
@AllArgsConstructor
public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID ownerId;
    private final Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private String name = "";
    private ChannelType type;

    @Builder.Default
    private String description = "";

    public Channel(String name, String description, ChannelType type, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.name = name;
        this.type = type;
        this.description = description;
    }

    // 필드를 수정하는 update 함수
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
        System.out.println("채널의 정보가 수정되었습니다. 수정시간: " + getUpdatedAt());
    }

    @Override
    public String toString() {
        return "채널명 : " + name + ", 채널 타입 : " + type;
    }
}
