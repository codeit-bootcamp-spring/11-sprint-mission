package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class UserChannel extends BaseEntity {

    private UUID userId;
    private UUID channelId;
    private UserChannelRole userChannelRole;

    private UserChannel(UUID userId, UUID channelId, UserChannelRole userChannelRole) {
        super();
        this.userId = userId;
        this.channelId = channelId;
        this.userChannelRole = userChannelRole;
    }

    protected UserChannel(UserChannel other) {
        super(other);
        this.userId = other.userId;
        this.channelId = other.channelId;
        this.userChannelRole = other.userChannelRole;
    }

    public static UserChannel create(UUID userId, UUID channelId, UserChannelRole userChannelRole) {
        return new UserChannel(userId, channelId, userChannelRole);
    }

    @Override
    public UserChannel copy() {
        return new UserChannel(this);
    }
}
