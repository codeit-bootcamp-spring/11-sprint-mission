package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String content;
    private UUID channelId;
    private UUID authorId;
    private List<UUID> attachmentIds;
    private List<MessageEditHistory> editHistories;
    private boolean isDeleted;

    public Message(String content, UUID channelId, UUID authorId) {
        super();
        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
        this.attachmentIds = new ArrayList<>();
        this.editHistories = new ArrayList<>();
        this.isDeleted = false;
    }

    public Message(String content, UUID channelId, UUID authorId, List<UUID> attachmentIds) {
        super();
        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
        this.attachmentIds = new ArrayList<>(attachmentIds);
        this.editHistories = new ArrayList<>();
        this.isDeleted = false;
    }

    public void setContent(String newContent) {
        MessageEditHistory history = new MessageEditHistory(this.content);
        this.editHistories.add(history);
        this.content = newContent;
        setUpdatedAt(Instant.now());
    }

    public void delete() {
        this.isDeleted = true;
        setUpdatedAt(Instant.now());
    }

    public void update(String content) {
        setContent(content);
    }
}