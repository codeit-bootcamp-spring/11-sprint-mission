package com.sprint.mission.discodeit.entity.Domain;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class Message extends BaseEntity implements Serializable {
    private String messageContent;     // 내용
    private User messageSender;        // 보낸이
    private User messageReceiver;     // 받는이
    private static final long serialVersionUID = 1L;

    public Message(String messageContent, User messageSender, User messageReceiver) {
        super();
        this.messageContent = messageContent;
        this.messageSender = messageSender;
        this.messageReceiver = messageReceiver;
    }

    public void updateContent( String messageContent) {
        this.messageContent = messageContent;
        updateTimestamp();
    }

    @Override
    public String toString() {
        return "Message ---- [" +
                "id = " + id +
                "] [sender='" + messageSender + '\'' +
                "], [receiver='" + messageReceiver + '\'' +
                "], [content='" + messageContent + "\']" +
                " [createdAt='" + createdAt + "']" +
                " [updatedAt='" + updatedAt + "']";

    }
}
