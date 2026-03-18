package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.CreateBinaryContentRequestDTO;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContent create(
            CreateBinaryContentRequestDTO dto
    ) {
        BinaryContent newBinaryContent = BinaryContent.create(
                dto.fileName(),
                dto.contentType(),
                dto.data()
        );

        return binaryContentRepository.save(newBinaryContent);
    }

    @Override
    public BinaryContent find(UUID binaryContentId) {
        return binaryContentRepository.findById(binaryContentId)
                .orElseThrow(() -> new RuntimeException("해당 BinaryContent는 없습니다."));
    }

    @Override
    public List<BinaryContent> findAllByIdIn(
            List<UUID> ids
    ) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
                .map(id -> binaryContentRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void delete(
            UUID binaryContentId
    ) {
        BinaryContent binaryContent = find(binaryContentId);

        binaryContentRepository.deleteById(binaryContentId);
    }
}
