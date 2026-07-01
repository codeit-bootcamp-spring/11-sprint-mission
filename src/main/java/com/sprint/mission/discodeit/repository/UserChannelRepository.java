package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserChannel;
import com.sprint.mission.discodeit.repository.base.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserChannelRepository extends Repository<UserChannel, UUID> {

    Optional<UserChannel> findByUserIdAndChannelId(UUID userId, UUID channelId);

    List<UserChannel> findAllByUserId(UUID userId);

    List<UserChannel> findAllByChannelId(UUID channelId);
}
