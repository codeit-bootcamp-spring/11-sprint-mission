package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {
    BinaryContent create(BinaryContentCreateRequest request);

    BinaryContent read(UUID id);

    // 메세지에 첨부된 다건의 파일 한번에 조회
    List<BinaryContent> readAllByIdIn(List<UUID> ids);

    void delete(UUID id);
}
