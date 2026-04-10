package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @Query("""
        select m.channel.id, max(m.createdAt)
        from Message m
        where m.channel in :channels
        group by m.channel.id
    """)
    List<Object[]> findLastMessageAtByChannels(@Param("channels") List<Channel> channel);

    @EntityGraph(attributePaths = "attachments")
    List<Message> findAllWithAttachmentsByChannel(Channel channel);

    List<Message> findTop51ByChannelOrderByCreatedAtDesc(Channel channel);
    List<Message> findTop51ByChannelAndCreatedAtLessThanOrderByCreatedAtDesc(Channel channel, Instant createdAt);
}
