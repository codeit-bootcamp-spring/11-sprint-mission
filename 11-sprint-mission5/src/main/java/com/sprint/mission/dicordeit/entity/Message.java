package com.sprint.mission.dicordeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
    private UUID id;
    private String content;
    private UUID channelId;
    private UUID senderId;
    private long createdAt;
    private long updatedAt;

    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public Message(String content, UUID channelId, UUID senderId){
        this.id = UUID.randomUUID();
        this.content = content;
        this.channelId = channelId;
        this.senderId = senderId;

        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;

    }
    public void update(String newContent){
        this.content = newContent;
        this.updatedAt = System.currentTimeMillis();
    }

}
