package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  @Override
  @EntityGraph(attributePaths = {"user", "channel"})
  Optional<ReadStatus> findById(UUID id);

  Optional<ReadStatus> findByUserAndChannel(User user, Channel channel);

  @EntityGraph(attributePaths = {"user"})
  Optional<ReadStatus> findByChannelId(UUID id);

  void deleteAllByChannelId(UUID id);

  @EntityGraph(attributePaths = {"user", "channel"})
  List<ReadStatus> findAllByUserIdIn(List<UUID> userIds);
}
