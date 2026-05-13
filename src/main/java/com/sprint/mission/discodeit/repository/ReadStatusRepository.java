package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {
    List<ReadStatus> findAllByUser_Id(UUID userId);
    List<ReadStatus> findAllByChannel_Id(UUID channelId);
    boolean existsByUser_IdAndChannel_Id(UUID userId, UUID channelId);

    @Query("""
        select distinct rs
        from ReadStatus rs
        join fetch rs.user u
        left join fetch u.profile
        left join fetch u.status
        where rs.channel.id in :channelIds
    """)
    List<ReadStatus> findAllWithUserByChannelIds(@Param("channelIds") List<UUID> channelIds);
}