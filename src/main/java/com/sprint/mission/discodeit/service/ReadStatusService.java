package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    List<ReadStatus> findAllByUserId(UUID userId);
    ReadStatus createReadStatus(ReadStatusCreateRequest request);
    ReadStatus updateReadStatus(UUID readStatusId, ReadStatusUpdateRequest request);
}