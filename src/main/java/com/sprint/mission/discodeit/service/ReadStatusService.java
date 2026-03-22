package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatusResponse createReadStatus(ReadStatusCreateRequest readStatusCreateRequest);
    ReadStatusResponse findById(UUID id);
    List<ReadStatusResponse> findAllByUserId(UUID userId);
    ReadStatusResponse updateReadStatus(ReadStatusUpdateRequest readStatusUpdateRequest);

    void deleteReadStatus(UUID id);
}
