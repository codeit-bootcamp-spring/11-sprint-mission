package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.Collection;
import java.util.Collections;
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

  @Query("""
      SELECT m FROM Message m
      WHERE m.channel.id IN :channelIds
      AND m.createdAt = (
            SELECT MAX(m2.createdAt)
            FROM Message m2
            WHERE m2.channel.id = m.channel.id
            )""")
  List<Message> findLatestMessagesByChannelIds(@Param("channelIds") Collection<UUID> channelIds);

  Optional<Message> findTopByChannelIdOrderByCreatedAtDesc(UUID channelId);

  @EntityGraph(attributePaths = {"author", "author.status", "author.profile"})
  Slice<Message> findByChannelId(UUID channelId, Pageable pageable);
}
