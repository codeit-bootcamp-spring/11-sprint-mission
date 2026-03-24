package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentRepository {

   BinaryContent saveBinaryContent(BinaryContent binaryContent);
   Optional<BinaryContent> getBinaryContent(UUID binaryContentId);
   Optional<BinaryContent> getProfileContentByUserId(UUID userId);
   List<BinaryContent> getAllBinaryContent();
   List<BinaryContent> getAllByUserId(UUID userId);
   List<BinaryContent> getAllByMessageId(UUID messageId);
   boolean deleteBinaryContent(UUID binaryContentId);
   boolean deleteBinaryContentByMessageId(UUID messageId);
   boolean isExistBinaryContent(UUID binaryContentId);









}
