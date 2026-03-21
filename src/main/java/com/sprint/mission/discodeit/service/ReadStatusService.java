package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatus.CreateReadStatusRequestDTO;
import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {
    ReadStatus create(
            CreateReadStatusRequestDTO dto
    );

    ReadStatus find(
            UUID readStatusId
    );

    List<ReadStatus> findAllByUserId(
            UUID userId
    );

    ReadStatus update(
            UUID readStatusId
    );

    void delete(
            UUID readStatusId
    );
}
