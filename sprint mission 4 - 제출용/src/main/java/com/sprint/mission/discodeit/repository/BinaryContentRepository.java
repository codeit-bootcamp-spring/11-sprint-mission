package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentRepository {

  BinaryContent create(BinaryContent binaryContent);

  BinaryContent readById(UUID id);

  BinaryContent readByUserId(UUID userId);

  List<BinaryContent> readAllByMessageId(UUID messageId);

  void delete(UUID id);

  void deleteByMessageId(UUID messageId);

  void deleteByUserId(UUID userId);
}
