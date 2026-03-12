package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.BinaryContent;
import java.util.List;
import java.util.UUID;

public interface BinaryContentService {
    BinaryContent createBinaryContent(String filename, Long size, String contentType, byte[] bytes);
    BinaryContent getBinaryContentById(UUID id);
    List<BinaryContent> getAllBinaryContents();
    void deleteBinaryContent(UUID id);
}
