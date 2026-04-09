package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  ReadStatus save(ReadStatus status);

  List<ReadStatus> findAllByChannelId(UUID id);

  List<ReadStatus> findAllByUserId(UUID id);

  void deleteAllByChannelId(UUID id);

  Optional<ReadStatus> findById(UUID id);

  void deleteById(UUID id);

  Optional<ReadStatus> findByUserIdAndChannelId(UUID authorId, UUID channelId);
}
