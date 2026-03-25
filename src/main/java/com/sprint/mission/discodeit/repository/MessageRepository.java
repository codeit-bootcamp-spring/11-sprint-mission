package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {
    void insert(Message message);

    Message findById(UUID id);
    List<Message> findAllByChannelId(UUID channelId); // 채널을 찾으면 해당 채널의 모든 메시지를 조회한다.

    void update(Message message);

    void delete(UUID id);
    void deleteAllByChannelId(UUID channelId); // 채널이 삭제되면 채널 내 메시지도 삭제된다

}
