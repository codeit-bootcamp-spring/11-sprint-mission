package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {

    @Query("""
        select distinct c
        from Channel c
        left join ReadStatus rs on rs.channel = c
        where c.type = com.sprint.mission.discodeit.entity.ChannelType.PUBLIC
           or rs.user.id = :userId
    """)
    List<Channel> findVisibleChannelsByUserId(@Param("userId") UUID userId);
}