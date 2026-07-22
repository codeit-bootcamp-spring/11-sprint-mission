package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPAReadStatusRepository extends JpaRepository<ReadStatus, UUID> {


  @EntityGraph(attributePaths = {"user", "channel", "user.profile"})
  List<ReadStatus> findAllByUserId(UUID userId);

  @EntityGraph(attributePaths = {"user", "channel", "user.profile"})
  List<ReadStatus> findAllByChannel_Id(UUID channelId);


  Boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);

  @EntityGraph(attributePaths = {"user", "channel", "user.profile"})
  List<ReadStatus> findAllByChannel_IdIn(List<UUID> channelIds);

  List<ReadStatus> findAllByChannelIdAndNotificationEnabledTrue(UUID uuid);
}
