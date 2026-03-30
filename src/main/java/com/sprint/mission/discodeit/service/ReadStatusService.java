package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponseDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequestDto;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatusResponseDto create(UUID channelId, ReadStatusCreateRequestDto dto);
    ReadStatusResponseDto find(UUID id);
    List<ReadStatusResponseDto> findAllByUserId(UUID id);
    void update(UUID id);
    void delete(UUID id);
    void updateByChannelId(UUID channelId, ReadStatusUpdateRequestDto dto);
}

