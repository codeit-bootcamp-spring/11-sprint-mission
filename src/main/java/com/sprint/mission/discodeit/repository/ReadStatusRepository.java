package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  List<ReadStatus> findByChannelId(UUID channelId);

  List<ReadStatus> findByUserId(UUID userId);

  void deleteByChannelId(UUID channelId);

  boolean existsByUserIdAndChannelId(UUID userId, UUID channelId);
}
