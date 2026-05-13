package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.messagedto.LastMessageTimeDto;
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


public interface JPAMessageRepository extends JpaRepository<Message, UUID> {


  @EntityGraph(attributePaths = {"author", "channel", "author.status", "author.profile"})
  @Query("SELECT m FROM Message m "
      + "WHERE (:cursor IS NULL OR m.createdAt < :cursor) "
      + " AND m.channel.id = :channelId")
  Slice<Message> findAllByChannel_Id(UUID channelId, Pageable pageable, Instant cursor);


  @EntityGraph(attributePaths = {"author", "channel", "author.status", "author.profile"})
  Slice<Message> findAllByChannel_Id(UUID channelId, Pageable pageable);


  @EntityGraph(attributePaths = {"author", "channel", "author.status", "author.profile"})
  Optional<Message> findTopByChannel_IdOrderByCreatedAtDesc(UUID channelId);


  @Query("SELECT m.channel.id, MAX(m.createdAt) FROM Message m WHERE m.channel.id IN :channelIds GROUP BY m.channel.id")
  List<LastMessageTimeDto> findAllLastMessageAtByChannel_Id(List<UUID> channelIds);

  void deleteById(UUID id);
}
