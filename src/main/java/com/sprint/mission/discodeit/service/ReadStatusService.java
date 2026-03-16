package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.ReadStatusResponseDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequestDto;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatusResponseDto create(ReadStatusCreateRequestDto dto);
    ReadStatusResponseDto find(UUID id);
    List<ReadStatusResponseDto> findAllByUserId(UUID id);
    void update(ReadStatusUpdateRequestDto dto);
    void delete(UUID id);
}

