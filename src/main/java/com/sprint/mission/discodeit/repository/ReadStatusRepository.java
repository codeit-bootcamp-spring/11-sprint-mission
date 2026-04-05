package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {

  void save(ReadStatus readStatus);

  Optional<ReadStatus> findById(UUID id);

  List<ReadStatus> findAll();

  List<ReadStatus> findAllByUserId(UUID userId);

  List<ReadStatus> findAllByChannelId(UUID channelId);

  List<ReadStatus> findAllByChannelIdIn(List<UUID> channelIds);

  boolean existByUserIdAndChannelId(UUID userId, UUID channelId);

  void delete(ReadStatus readStatus);

  void deleteAllByUserId(UUID userId);

  void deleteAllByChannelId(UUID channelId);
}
