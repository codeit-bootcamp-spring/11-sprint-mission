package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
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

  @EntityGraph(attributePaths = {"author", "author.profile"})
  Slice<Message> findAllByChannelIdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

  @EntityGraph(attributePaths = {"author", "author.profile"})
  Slice<Message> findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(UUID channelId,
      Instant cursor,
      Pageable pageable);

  void deleteAllByChannel(Channel channel);

  @Query("SELECT m.createdAt FROM Message m WHERE m.channel = :channel ORDER BY m.createdAt DESC LIMIT 1")
  Optional<Instant> findTopCreatedAtByChannelOrderByCreatedAtDesc(
      @Param("channel") Channel channel);

  @Query("SELECT m.channel.id AS channelId, MAX(m.createdAt) AS lastMessageAt FROM Message m WHERE m.channel.id IN :channelIds GROUP BY m.channel.id")
  List<ChannelResponse.LastMessageAt> findLastMessageAtByChannelIds(
      @Param("channelIds") List<UUID> channelIds);
}