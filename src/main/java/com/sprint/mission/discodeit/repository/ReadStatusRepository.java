package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Locked.Read;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  List<ReadStatus> findAllByChannelId(UUID id);

  List<ReadStatus> findAllByUserId(UUID id);

  @EntityGraph(attributePaths = {"user", "user.profile"})
  List<ReadStatus> findAllByChannelIdIn(Collection<UUID> channelIds);

  Optional<ReadStatus> findByUserIdAndChannelId(UUID authorId, UUID channelId);

  List<ReadStatus> findAllByChannelIdAndNotificationEnabledTrue(UUID channelId);
}