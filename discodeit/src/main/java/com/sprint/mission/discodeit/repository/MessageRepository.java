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

    List<Message> findAllByChannelId(UUID channelId);

    // 커서 없음 (첫 페이지): createdAt DESC 정렬
    @EntityGraph(attributePaths = {"author", "author.profile", "author.status"})
    Slice<Message> findAllByChannelIdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

    // 커서 있음 (이후 페이지): cursor보다 오래된 메시지 조회
    @EntityGraph(attributePaths = {"author", "author.profile", "author.status"})
    Slice<Message> findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            UUID channelId, Instant createdAt, Pageable pageable);

    // 채널의 최신 메시지 시각만 조회 (전체 메시지 로딩 방지)
    @Query("SELECT MAX(m.createdAt) FROM Message m WHERE m.channel.id = :channelId")
    Optional<Instant> findLatestCreatedAtByChannelId(@Param("channelId") UUID channelId);

    void deleteAllByChannelId(UUID channelId);
}
