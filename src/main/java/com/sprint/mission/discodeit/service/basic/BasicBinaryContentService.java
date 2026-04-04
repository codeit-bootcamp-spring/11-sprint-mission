package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
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
    public BinaryContent create(BinaryContentCreateRequest dto) {
        BinaryContent binaryContent = dto.toBinaryContent();
        binaryContentRepository.insert(binaryContent);

        return binaryContent;
    }

    @Override
    public BinaryContent find(UUID id) {
        return binaryContentRepository.findById(id);
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return ids.stream().map(binaryContentRepository::findById).toList();
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.delete(id);
    }
}