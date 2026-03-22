package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {
    BinaryContentResponse createBinaryContent(BinaryContentCreateRequest binaryContentCreateRequest);

    BinaryContentResponse findById(UUID id);

    List<BinaryContentResponse> findAllByIdIn(List<UUID> ids);

    void deleteBinaryContent(UUID id);
}
