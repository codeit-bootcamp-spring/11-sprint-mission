package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {
    Message create(Message message);

    Message read(UUID id);

    List<Message> readAll();
    List<Message> readAllByChannelId(UUID channelId);

    Message update(Message message);

    void delete(UUID id);
    void deleteAllByChannelId(UUID channelId);  // 추가
}
