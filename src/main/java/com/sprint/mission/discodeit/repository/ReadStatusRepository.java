package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  @Query("SELECT r FROM ReadStatus r "
      // readStatus는 user가 삭제되면 cascade 조건이므로, readStatus가 존재하면 유저도 존재함.
      + "JOIN FETCH r.user u "
      + "LEFT JOIN FETCH u.profile "
      + "WHERE r.channel.id = :channelId")
  List<ReadStatus> findAllByChannelIdWithUserWithProfile(
      @Param("channelId") UUID channelId);

  @EntityGraph(attributePaths = {"channel"})
  List<ReadStatus> findAllByUserId(UUID userId);

  boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);

}
