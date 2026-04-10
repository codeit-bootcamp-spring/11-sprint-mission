package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {
    Optional<ReadStatus> findByUserAndChannel(User user, Channel channel);

    @Query("""
        select rs.user
        from ReadStatus rs
        where rs.channel = :channel
    """)
    List<User> findUsersByChannel(@Param("channel") Channel channel);

    @Query("""
        select rs.channel.id, rs.user
        from ReadStatus rs
        where rs.channel in :channels
    """)
    List<Object[]> findUsersByChannels(@Param("channels") List<Channel> channel);

    @EntityGraph(attributePaths = {"user", "channel"})
    List<ReadStatus> findAllWithUserAndChannelByUser(User user);
}
