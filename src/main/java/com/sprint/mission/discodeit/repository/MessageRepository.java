package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.projection.ChannelLastMessageAtProjection;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    Optional<Instant> findLastMessageAtByChannel(@Param("channel")Channel channels);

    @Query("""
        select new com.sprint.mission.discodeit.dto.projection.ChannelLastMessageAtProjection(
            m.channel.id,
            max(m.createdAt)
        )
        from Message m
        where m.channel in :channels
        group by m.channel.id
    """)
    List<ChannelLastMessageAtProjection> findLastMessageAtByChannels(@Param("channels") List<Channel> channels);

    @EntityGraph(attributePaths = "attachments")
    List<Message> findAllWithAttachmentsByChannel(Channel channel);

    @EntityGraph(attributePaths = {
            "channel",
            "author",
            "author.status",
            "author.profile",
            "attachments"
    })
    List<Message> findAllByChannelOrderByCreatedAtDesc(Channel channel, Pageable pageable);

    @EntityGraph(attributePaths = {
            "channel",
            "author",
            "author.status",
            "author.profile",
            "attachments"
    })
    List<Message> findAllByChannelAndCreatedAtLessThanOrderByCreatedAtDesc(
            Channel channel, Instant createdAt, Pageable pageable
    );

    default List<Message> findAllByChannelWithCursor(
            Channel channel, Instant cursor, int pageSize
    ) {
        PageRequest pageRequest = PageRequest.of(0, pageSize + 1);
        return cursor == null
                ? findAllByChannelOrderByCreatedAtDesc(channel, pageRequest)
                : findAllByChannelAndCreatedAtLessThanOrderByCreatedAtDesc(channel, cursor, pageRequest);
    }

    @EntityGraph(attributePaths = {
            "channel",
            "author",
            "author.status",
            "author.profile",
            "attachments"
    })
    @Query("select m from Message m where m.id = :id")
    Optional<Message> findWithDetailsById(@Param("id") UUID id);
}
