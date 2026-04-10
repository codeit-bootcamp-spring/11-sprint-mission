package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  List<ReadStatus> findAllByUserId(UUID userId);

  List<ReadStatus> findAllByChannel(Channel channel);

  List<ReadStatus> findAllByChannelIn(List<Channel> channels);

  boolean existsByUserAndChannel(User user, Channel channel);

  void deleteAllByChannel(Channel channel);

  void deleteAllByUser(User user);
}
