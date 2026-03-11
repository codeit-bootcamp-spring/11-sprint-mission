package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;

public interface ChannelRepository {

    boolean saveChannel(Channel channel);
    Channel getChannel(String channelId);
    List<Channel> getAllChannel();
    boolean updateChannel(Channel channel);
    boolean deleteChannel(String channelId);
    boolean isExistChannel(String channelId);




}
