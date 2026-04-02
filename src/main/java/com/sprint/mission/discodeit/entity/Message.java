package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends BaseEntity {
    private String content;
    private final UUID senderId;
    private final UUID channelId;
    private List<UUID> attachmentIds;

    public Message(String content, UUID senderId, UUID channelId, List<UUID> attachmentIds) {
        this.content = content;
        this.senderId = senderId;
        this.channelId = channelId;
        this.attachmentIds = new ArrayList<>(attachmentIds);
    }

    public void updateContent(String content) {
        this.content = content;
        this.setUpdatedAt();
    }

    public void replaceAttachments(List<BinaryContent> attachments) {
        this.attachmentIds = new ArrayList<>(
                attachments.stream()
                        .map(BinaryContent::getId)
                        .toList()
        );
        this.setUpdatedAt();
    }
}