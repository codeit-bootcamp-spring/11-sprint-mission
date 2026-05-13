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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  // Pageable, 몇 번째 페이지(page)의 몇 개의 데이터(size)로 나타낼 지 ?(오프셋 방식)
  @EntityGraph(attributePaths = {"author"})
  Slice<Message> findByChannelIdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

  @EntityGraph(attributePaths = {"author"})
  @Query("""
      SELECT m 
      FROM Message m 
      WHERE m.channel.id = :channelId 
      AND (m.createdAt < :cursor OR :cursor IS NULL)
      ORDER BY m.createdAt DESC
      """)
  Slice<Message> findMessages(@Param("channelId") UUID channelId, @Param("cursor") Instant cursor,
      @Param("pageable") Pageable pageable);

  @Query("""
      SELECT m FROM Message m
            WHERE m.createdAt =
                  (SELECT MAX(m2.createdAt) FROM Message m2 WHERE m2.channel.id = m.channel.id)
                  AND m.channel.id IN :channelIds
      """)
  List<Message> findLastMessagesByChannelIds(List<UUID> channelIds);

  Optional<Message> findTopByChannelIdOrderByCreatedAtDesc(UUID channelId);

  void deleteAllByChannelId(UUID id);
}
