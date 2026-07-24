package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  @EntityGraph(attributePaths = {"channel"})
  List<ReadStatus> findAllByUserId(UUID userId);

  @EntityGraph(attributePaths = {"user", "user.profile"})
  List<ReadStatus> findAllByChannel(Channel channel);

  @EntityGraph(attributePaths = {"user", "user.profile"})
  List<ReadStatus> findAllByChannelIn(List<Channel> channels);

  @EntityGraph(attributePaths = {"user"})
  List<ReadStatus> findAllByChannelIdAndNotificationEnabledTrue(UUID channelId);

  boolean existsByUserAndChannel(User user, Channel channel);

  void deleteAllByChannel(Channel channel);

  void deleteAllByUser(User user);
}
