package com.sprint.mission.discodeit.entity;

import lombok.Getter;

@Getter
public class Message extends BaseEntity {
    private String content;
    private User sender;
    private Channel channel;

    public Message(String content, User sender, Channel channel) {
        this.content = content;
        this.sender = sender;
        this.channel = channel;
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