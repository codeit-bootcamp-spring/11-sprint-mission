package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Message extends BaseEntity {
    private String content;
    private final UUID authorId;
    private final UUID channelId;

    @Builder.Default // 빈 값으로 빌드
    private List<UUID> attachmentIds = new ArrayList<>();


    // 메시지 내용만 수정 가능
    public void update(String newContent) {
        boolean anyValueUpdated = false;

        if (newContent != null && !newContent.equals(this.content)) {
            this.content = newContent;
            anyValueUpdated = true;
        }

        if (anyValueUpdated) {
            super.timeUpdate();
        }
    }

    // 첨부파일 추가
    public void addAttachment(UUID attachmentId) {
        this.attachmentIds.add(attachmentId);
        super.timeUpdate();
    }

    @Override
    public String toString() {
        return "Message [" +
                "UUID: " + getId() +
                "\n발신자 ID: " + getAuthorId() +
                ", 채널 ID: " + getChannelId() +
                ", 내용: " + getContent() +
                ", 작성 시간: " + getCreatedAt() +
                ", 수정 시간: " + getUpdatedAt() +
                "]\n";
    }
}