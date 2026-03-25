package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatusDto create(ReadStatusCreateRequest request);
    Optional<ReadStatusDto> find(UUID id);
    List<ReadStatusDto> findAllByUserId(UUID userId);
    ReadStatusDto update(ReadStatusUpdateParam param);
    void delete(UUID id);
}