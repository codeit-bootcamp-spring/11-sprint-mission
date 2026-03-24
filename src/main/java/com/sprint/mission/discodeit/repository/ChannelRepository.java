package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository {

    boolean saveChannel(Channel channel);
    Optional<Channel> getChannel(UUID channelId);
    List<Channel> getAllChannel();

    boolean deleteChannel(UUID channelId);
    boolean isExistChannel(UUID channelId);




}
