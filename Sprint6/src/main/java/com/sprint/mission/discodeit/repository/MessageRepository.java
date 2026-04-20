package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository; // 추가
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findAllByChannelId(UUID channelId);

  void deleteAllByChannelId(UUID channelId);
}