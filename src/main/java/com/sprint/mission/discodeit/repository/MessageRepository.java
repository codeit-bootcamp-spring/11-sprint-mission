package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  Optional<Message> findFirstByChannelIdOrderByCreatedAtDesc(UUID channelId);

  List<Message> findAllByChannelId(UUID id); // 사용되는 곳 없음

  @EntityGraph(attributePaths = {"author"})
  Slice<Message> findByChannelIdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

  @EntityGraph(attributePaths = {"author"})
  Slice<Message> findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(UUID channelId,
      Instant cursor, Pageable pageable);
}