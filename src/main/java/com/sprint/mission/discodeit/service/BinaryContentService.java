package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.binaryContent.CreateBinaryContentRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {
    BinaryContent createBinaryContent(CreateBinaryContentRequest request);
    BinaryContent getBinaryContentById(UUID id);
    List<BinaryContent> findAllByIdIn(List<UUID> ids);
    void deleteBinaryContent(UUID id);
}