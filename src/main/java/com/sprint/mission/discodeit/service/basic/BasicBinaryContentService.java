package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContent createBinaryContent(String filename, Long size, String contentType, byte[] bytes) {
        BinaryContent binaryContent = new BinaryContent(filename, size, contentType, bytes);
        binaryContentRepository.save(binaryContent);
        return binaryContent;
    }

    @Override
    public BinaryContent getBinaryContentById(UUID id) {
        return binaryContentRepository.findById(id);
    }

    @Override
    public List<BinaryContent> getAllBinaryContents() {
        return binaryContentRepository.findAll();
    }

    @Override
    public void deleteBinaryContent(UUID id) {
        binaryContentRepository.deleteById(id);
    }
}
