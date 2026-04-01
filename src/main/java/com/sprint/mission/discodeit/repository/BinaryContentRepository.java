package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinaryContentRepository {
    BinaryContent save(BinaryContent content);
    Optional<BinaryContent> findById(UUID id);
    List<BinaryContent> findAllById(Collection<UUID> ids);
    void delete(UUID id);
    void deleteAllByAttachmentIds(List<UUID> attachmentIds);
}
