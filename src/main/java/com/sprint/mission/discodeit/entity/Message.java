package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Message extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private User sender;
    private User receiver;
    private String content;
    private Channel channel;
    private List<MessageEditHistory> editHistories;
    private boolean isDeleted;

    public Message(User sender, User receiver, String content) {
        super();
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.channel = null;
        this.editHistories = new ArrayList<>();
        this.isDeleted = false;
    }

    public Message(User sender, Channel channel, String content) {
        super();
        this.sender = sender;
        this.receiver = null;
        this.content = content;
        this.channel = channel;
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

    public boolean isDM() {
        return channel == null;
    }

    public void update(String content) {
        setContent(content);
    }
}
