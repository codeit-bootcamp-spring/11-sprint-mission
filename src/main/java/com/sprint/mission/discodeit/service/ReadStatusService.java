package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.readStatus.CreateReadStatusRequest;
import com.sprint.mission.discodeit.dto.request.readStatus.UpdateReadStatusRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatus createReadStatus(CreateReadStatusRequest request);
    ReadStatus getReadStatusById(UUID id);
    List<ReadStatus> getReadStatusesByUserId(UUID userId);
    void updateReadStatus(UUID id, UpdateReadStatusRequest request);
    void deleteReadStatus(UUID id);
}