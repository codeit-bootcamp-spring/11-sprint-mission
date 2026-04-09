package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query("""
        select max(m.createdAt)
        from Message m
        where m.channel = :channel
    """)
    Optional<Instant> findLastMessageAtByChannel(@Param("channel")Channel channel);

    List<Message> findAllByChannel(Channel channel);
    Slice<Message> findAllByChannelOrderByCreatedAtDesc(Channel channel, Pageable pageable);
}
