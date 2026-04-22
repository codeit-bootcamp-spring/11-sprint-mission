package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  List<ReadStatus> findAllByUserId(UUID userId);

  List<ReadStatus> findAllByChannelId(UUID channelId);

  boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);

  void deleteByChannelId(UUID id);

  @Query("SELECT rs FROM ReadStatus rs "
      + "JOIN FETCH rs.channel "
      + "JOIN FETCH rs.user "
      + "WHERE rs.user.id = :userId")
  List<ReadStatus> findAllByUserIdWithChannelAndUser(@Param("userId") UUID userId);
}