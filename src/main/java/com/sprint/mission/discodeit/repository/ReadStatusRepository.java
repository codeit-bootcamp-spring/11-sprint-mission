package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {
    Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId);

    @Query("""
        select rs.user.id
        from ReadStatus rs
        where rs.channel.id = :channelId
    """)
    List<UUID> findUserIdsByChannelId(@Param("channelId") UUID channelId);

    List<ReadStatus> findAllByUser(User user);
}
