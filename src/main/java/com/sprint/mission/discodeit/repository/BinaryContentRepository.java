package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.UUID;

public interface BinaryContentRepository {
    void insert(BinaryContent binaryContent);
    BinaryContent findById(UUID id);
    void delete(UUID id);
}
