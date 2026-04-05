package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseEntity {
    private ChannelType type;
    private String name;
    private String description;
    private List<UUID> memberIds;

    private Channel(ChannelType type, String name, String description, List<UUID> memberIds) {
        super();
        this.type = type;
        this.name = name;
        this.description = description;
        this.memberIds = memberIds != null ? new ArrayList<>(memberIds) : new ArrayList<>();
    }

    // PUBLIC 채널 생성 메서드
    public static Channel createPublic(String name, String description) {
        return new Channel(ChannelType.PUBLIC, name, description, null);
    }

    // PRIVATE 채널 생성 메서드
    public static Channel createPrivate(List<UUID> memberIds) {
        return new Channel(ChannelType.PRIVATE, null, null, memberIds);
    }

    public void update(String newName, String newDescription) {
        boolean anyValueUpdated = false;

        if (newName != null && !newName.equals(this.name)) {
            this.name = newName;
            anyValueUpdated = true;
        }
        if (newDescription != null && !newDescription.equals(this.description)) {
            this.description = newDescription;
            anyValueUpdated = true;
        }

        if (anyValueUpdated) {
            super.timeUpdate();
        }
    }

    @Override
    public String toString() {
        return "Channel [" +
                "UUID: " + getId() +
                "\n이름: " + getName() +
                ", 설명: " + getDescription() +
                ", 타입: " + getType().getName() +
                ", 참여 인원: " + (getMemberIds() != null ? getMemberIds().size() : 0) + "명" +
                ", 생성 시간: " + getCreatedAt() +
                ", 수정 시간: " + getUpdatedAt() +
                "]\n";
    }


}