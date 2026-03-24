package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readstatusdto.CreateReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatusdto.ReadStatusInfoDto;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {

    ReadStatusInfoDto create(CreateReadStatusDto createReadStatusDto);
    ReadStatusInfoDto find(CreateReadStatusDto createReadStatusDto);
    List<ReadStatusInfoDto> findAllById(UUID userId);

    boolean update(CreateReadStatusDto createReadStatusDto);
    boolean delete(CreateReadStatusDto createReadStatusDto);




}
