package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  Optional<Message> findFirstByChannelIdOrderByCreatedAtDesc(UUID channelId);

  @Query("SELECT m FROM Message m "
      + "JOIN m.channel c "
      + "LEFT JOIN FETCH m.author a " // 탈퇴한 유저 존재 가능성 고려
      + "LEFT JOIN FETCH a.profile "
      + "WHERE c.id = :channelId "
      + "ORDER BY m.createdAt DESC")
  Slice<Message> findByChannelIdOrderByCreatedAtDesc(@Param("channelId") UUID channelId,
      Pageable pageable);

  @Query("SELECT m FROM Message m "
      + "JOIN m.channel c "
      + "LEFT JOIN FETCH m.author a "
      + "LEFT JOIN FETCH a.profile "
      + "WHERE c.id = :channelId "
      + "AND m.createdAt < :cursor " // 커서보다 과거 메시지 조회 조건
      + "ORDER BY m.createdAt DESC")
  Slice<Message> findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(
      @Param("channelId") UUID channelId,
      @Param("cursor") Instant cursor, Pageable pageable);
}