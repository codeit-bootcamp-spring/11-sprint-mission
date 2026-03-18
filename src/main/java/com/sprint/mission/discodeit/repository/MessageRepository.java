package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.base.Repository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends Repository<Message, UUID> {

    // 시간순 (최신순)
    List<Message> findAllByUserId(UUID userId);

    List<Message> findAllByChannelId(UUID channelId);
}
