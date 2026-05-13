package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("""
        select m.channel.id, max(m.createdAt)
        from Message m
        where m.channel.id in :channelIds
        group by m.channel.id
    """)
    List<Object[]> findLastMessageTimesByChannelIds(@Param("channelIds") List<UUID> channelIds);

    @Query("""
        select m.id
        from Message m
        where m.channel.id = :channelId
          and (
                :cursorCreatedAt is null
                or m.createdAt < :cursorCreatedAt
                or (m.createdAt = :cursorCreatedAt and m.id < :cursorId)
              )
        order by m.createdAt desc, m.id desc
    """)
    List<UUID> findPageIdsByChannelIdAndCursor(
            @Param("channelId") UUID channelId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );

    @Query("""
        select distinct m
        from Message m
        left join fetch m.author a
        left join fetch a.profile
        left join fetch a.status
        left join fetch m.attachments
        where m.id in :ids
        order by m.createdAt desc, m.id desc
    """)
    List<Message> findAllWithDetailsByIdIn(@Param("ids") List<UUID> ids);

    @Query("""
    select distinct m
    from Message m
    left join fetch m.author a
    left join fetch a.profile
    left join fetch a.status
    left join fetch m.attachments
    where m.id = :id
""")
    Optional<Message> findByIdWithDetails(@Param("id") UUID id);
}