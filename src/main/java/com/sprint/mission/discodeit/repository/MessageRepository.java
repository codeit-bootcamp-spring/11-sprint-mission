package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  void deleteByChannelId(UUID channelId);

  @EntityGraph(attributePaths = {"author"})
  Slice<Message> findByChannelIdOrderByCreatedAtDesc(
      UUID channelId, Pageable pageable);

  @EntityGraph(attributePaths = {"author"})
  Slice<Message> findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(
      UUID channelId, Instant createdAt, Pageable pageable);

}
