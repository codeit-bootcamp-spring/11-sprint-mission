package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findAllByChannelId(UUID channelId);

  List<Message> findAllByChannelIn(List<Channel> channels);

  void deleteAllByChannel(Channel channel);

  Optional<Instant> findTopCreatedAtByChannelOrderByCreatedAtDesc(Channel channel);
}
