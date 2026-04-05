package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.FileResponseDto;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public interface FileMessageService {
    public FileResponseDto findById(UUID fileId);

    public List<FileResponseDto> findAllByIds(List<UUID> ids);

    BinaryContent findBinaryContentById(UUID binaryContentId);
}
