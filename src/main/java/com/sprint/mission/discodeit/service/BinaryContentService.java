package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {
    BinaryContent create(MultipartFile file);
    List<BinaryContent> createAll(List<MultipartFile> files);
    BinaryContentDto find(UUID id);
    List<BinaryContentDto> findAllByIdIn(List<UUID> idList);
    void delete(BinaryContent binaryContent);
    void deleteAll(List<BinaryContent> binaryContents);
}
