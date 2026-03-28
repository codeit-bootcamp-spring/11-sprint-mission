package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatusResponse createReadStatus(ReadStatusCreateRequest readStatusCreateRequest);
    ReadStatusResponse findById(UUID id);
    List<ReadStatusResponse> findAllByUserId(UUID userId);
    ReadStatusResponse updateReadStatus(UUID readStatusUpdateRequest);
    void deleteReadStatus(UUID id);
}
