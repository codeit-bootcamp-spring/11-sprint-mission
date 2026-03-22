package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    void create(ReadStatusCreateRequest request);
    ReadStatus read(UUID id);
    List<ReadStatus> readAllByUserId(UUID userId);
    void update(UUID id, ReadStatusUpdateRequest request);
    void delete(UUID id);
}
