package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends BaseEntity {
    private String content;
    private User sender;
    private Channel channel;
    private List<UUID> attachments;

    public Message(String content, User sender, Channel channel) {
        this.content = content;
        this.sender = sender;
        this.channel = channel;
        this.attachments = new ArrayList<>();
    }

    public void updateContent(String content) {
        this.content = content;
        this.setUpdatedAt();
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + this.content + '\'' +
                ", sender=" + this.sender.getNickname() +
                ", channel=" + this.channel.getName() +
                '}';
    }
}