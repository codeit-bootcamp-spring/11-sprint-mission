package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.*;

@Getter
@ToString(callSuper = true)
public class Channel extends BaseEntity {

    private ChannelType type;
    private String name;
    private String description;
    private UUID masterUserId; // 방장
    private Instant recentMessageTime;

    private Channel(ChannelType type, String name, String description, UUID masterUserId, Instant recentMessageTime) {
        super();
        this.type = type;
        this.name = name;
        this.description = description;
        this.masterUserId = masterUserId;
        this.recentMessageTime = recentMessageTime;
    }

    protected Channel(Channel other) {
        super(other);
        this.type = other.type;
        this.name = other.name;
        this.description = other.description;
        this.masterUserId = other.masterUserId;
        this.recentMessageTime = other.recentMessageTime;
    }

    @Override
    public Channel copy() {
        return new Channel(this);
    }

    public static Channel create(ChannelType type, String name, String description, UUID masterUserId) {
        return new Channel(type, name, description, masterUserId, Instant.now());
    }

    // 이하 로직
    public void updateRecentMessageTime(Instant recentMessageTime) {
        this.recentMessageTime = recentMessageTime;
//        touch();
    }

    public void updateInfo(String name, String description, UUID requestUserId) {
        verifyChannelUpdate(requestUserId);
        this.name = name;
        this.description = description;
        touch();
    }

    public void verifyChannelUpdate(UUID requestUserId) {
        if (!this.masterUserId.equals(requestUserId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    public boolean isMaster(UUID userId) {
        return this.masterUserId.equals(userId);
    }

    public boolean isPrivate() {
        return this.type == ChannelType.PRIVATE;
    }
}
