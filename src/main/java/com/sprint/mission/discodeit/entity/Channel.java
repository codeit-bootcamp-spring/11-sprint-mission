package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;

    // 필드, 생성자, 메소드 등 정의
    private final UUID id; //고유식별자
    private final Instant createdAt; // 생성 시각
    private Instant updatedAt; //마지막 수정 시각
    private String name; // 채널 이름
    private String description; // 채널 설명
    private boolean isPrivate; // PUBLIC/PRIVATE 채널 여부
    private List<UUID> memberIds = new ArrayList<>(); // 참여자 목록
    private ChannelType type;

    // 생성자: id, createdAt, updatedAt을 생성자 내부에서 초기화, 나머지는 파라미터로 받기
    public Channel(String name, String description, boolean isPrivate) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate;
        if (memberIds != null) {
            this.memberIds = memberIds;
        }
        this.type = type;
    }

    // 외부 데이터(long 타입)에서 값을 받을 때 Instant 변환용 생성자
    public Channel(UUID id, long createdAt, long updatedAt, String name, String description) {
        this.id = id;
        this.createdAt = Instant.ofEpochMilli(createdAt);
        this.updatedAt = Instant.ofEpochMilli(updatedAt);
        this.name = name;
        this.description = description;
    }

    // Instant 타입을 바로 받는 변환용 생성자
    public Channel(UUID id, Instant createdAt, Instant updatedAt, String name, String description) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.name = name;
        this.description = description;
    }

    // 비즈니스 메서드 예시; 정보 업데이트
    public void update(String name, String description) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
        if (description != null && !description.isEmpty()) {
            this.description = description;
        }
        this.isPrivate = isPrivate;
        if (memberIds != null) {
            this.memberIds = memberIds;
        }
        if (type != null) {
            this.type = type;
        }
        this.updatedAt = Instant.now();
    }

    public Channel createChannel(String name) {
        return new Channel(name, "", false);
    }

    public enum ChannelType {
        PUBLIC, // 일반 채널
        PRIVATE  // 개인 채널

    }
}
