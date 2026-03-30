package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class Message extends BaseEntity {

    private String content;
    private final UUID channelId;
    private final UUID userId;
    private List<UUID> attachmentIds; // Set?

    private Message(String content, UUID channelId, UUID userId, List<UUID> attachmentIds) {
        super();
        this.content = content;
        this.channelId = channelId;
        this.userId = userId;
        this.attachmentIds = attachmentIds == null ? new ArrayList<>() : new ArrayList<>(attachmentIds);
    }

    protected Message(Message other) {
        super(other);
        this.content = other.content;
        this.userId = other.userId;
        this.channelId = other.channelId;
        this.attachmentIds = other.attachmentIds == null ? new ArrayList<>() :  new ArrayList<>(other.attachmentIds);
    }

    @Override
    public Message copy() {
        return new Message(this);
    }

    public static Message create(String content, UUID channelId, UUID userId, List<UUID> attachmentIds) {
        return new Message(content, channelId, userId, attachmentIds);
    }

    // 도메인 로직
    public void updateContent(String newContent, UUID requestUserId, List<UUID> attachmentIds) {
        verifySender(requestUserId);
        this.content = newContent;
        this.attachmentIds = attachmentIds == null ? new ArrayList<>() : new ArrayList<>(attachmentIds);
        touch();
    }

    public void verifySender(UUID requestUserId) {
        if (!this.userId.equals(requestUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}
