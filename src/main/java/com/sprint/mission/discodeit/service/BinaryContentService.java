package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {
    BinaryContent create(BinaryContentCreateRequest dto);
    BinaryContent find(UUID id);
    List<BinaryContent> findAllByIdIn(List<UUID> uuids);
    void delete(UUID id);
}
