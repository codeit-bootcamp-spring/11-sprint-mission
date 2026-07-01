package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatus.CreateReadStatusRequestDTO;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponseDTO;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatusResponseDTO create(
            CreateReadStatusRequestDTO dto
    );

    ReadStatusResponseDTO find(
            UUID readStatusId
    );

    List<ReadStatusResponseDTO> findAllByUserId(
            UUID userId
    );

    ReadStatusResponseDTO update(
            UUID readStatusId
    );

    void delete(
            UUID readStatusId
    );
}
