package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends BaseEntity {
    private final UUID authorId; // userId 에서 authorId로 변경
    // WHY? 단순 유저가 아니라, 메세지를 작성한 유저라는 뜻
    private final UUID channelId;
    private String content;
    private final List<UUID> attachmentIds;

    public Message(UUID authorId, UUID channelId, String content) {
        super();
        this.authorId = authorId;
        this.channelId = channelId;
        this.content = content;
        this.attachmentIds = new ArrayList<>();
    }

    public void updateContent(String content) {
        this.content = content;
        touch();
    }

    public void addAttachment(UUID attachmentId) {
        this.attachmentIds.add(attachmentId);
        touch();
    }

    public void removeAttachment(UUID attachmentId) {
        this.attachmentIds.remove(attachmentId);
        touch();
    }

    @Override
    public String toString() {
        return "Message{id = " + getId() +
                ", userId = " + authorId +
                ", channelId = " + channelId +
                ", content = '" + content + '\'' +
                '}';
    }
}
