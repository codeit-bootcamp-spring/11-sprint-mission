package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    //create, read, readAll, update, delete
    UUID createPublicChannel(PublicChannelCreateRequest request);
    UUID createPrivateChannel(PrivateChannelCreateRequest request);
    ChannelResponse read(UUID channelId);
    List<ChannelResponse> findAllByUserId(UUID userId);
    public void update(UUID channelId, ChannelUpdateRequest request);
    void delete(UUID id);

    void setMessageService(MessageService messageService);
    void setUserService(UserService userService);

    void deleteChannelByAdmin(UUID userId);

    void addUserToChannel(UUID userId, UUID channelId);
    void removeUserFromChannel(UUID userId, UUID channelId);
}
