package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Channel extends BaseEntity {
    private ChannelType type;
    private String name;
    private String description;
    private List<UUID> memberIds;

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