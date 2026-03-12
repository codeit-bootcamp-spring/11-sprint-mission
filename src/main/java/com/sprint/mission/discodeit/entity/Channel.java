package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID ownerId;
    private final long createdAt;
    private long updatedAt;
    private String name;
    private ChannelType type;
    private String description;

    // 생성자 초기화
    public Channel(String name, String description, ChannelType type, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.name = name;
        this.type = type;
        this.description = description;
    }

    // 필드를 수정하는 update 함수
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
        System.out.println("채널의 정보가 수정되었습니다. 수정시간: " + getUpdatedAt());
    }

    @Override
    public String toString() {
        return "채널명 : " + name + ", 채널 타입 : " + type;
    }
}
