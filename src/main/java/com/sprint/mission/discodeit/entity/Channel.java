package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class Channel implements Serializable {

    // 객체 직렬화
    private static final long serialVersionUID = 1L;

    // 필수
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    // 어디 그룹에 속한 채널인가
    private String group; // 채널 그룹
    private String name; // 채널 이름

    // 코드 탬플릿에 맞게 필드 추가
    private Type type;
    private String description;

    public enum Type {
        PUBLIC, PRIVATE;
    }

    // 생성자
    public Channel(String group, String name, String description) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.group = group;
        this.name = name;
        this.description = description;
    }

    // 정적 팩토리 메서드
    // 코드 탬플릿에 적합한 생성자 오버로딩
    private Channel(Type type, String name, String description) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.type = type;
        this.name = name;
        this.description = description;
    }

    // 정적 팩토리 메서드
    public static Channel create(Type channelType, String name, String description) {
        return new Channel(channelType, name, description);
    }

    // getter(Lombok의 @Getter로 대체)

    // update(set)
    private void update() {
        this.updatedAt = Instant.now();;
    }
    public void updateGroup(String group) {
        this.group = group;
        update();
    }
    public void updateName(String name) {
        this.name = name;
        update();
    }
    public void updateDescription(String description) {
        this.description = description;
        update();
    }

    @Override
    public String toString() {
        return "채널 UUID : " + id
                + "\n 생성 시간 : " + createdAt + ", 수정한 시간 : " + updatedAt
                + "\n 채널 이름 : " + name + ", 채널이 속해있는 그룹 : " + group;
    }
}
