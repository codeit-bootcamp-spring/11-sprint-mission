package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.BinaryContentCreateDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    public BinaryContent create(BinaryContentCreateDto dto) {
        if (dto.bytes() == null) {
            throw new IllegalArgumentException("저장할 이미지/파일이 없습니다.");
        }
        BinaryContent binaryContent = new BinaryContent(dto.bytes());
        return binaryContentRepository.save(binaryContent);
    }

    public BinaryContent find(UUID id) {
        return binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 binaryContent id 입니다."));
    }

    public List<BinaryContent> findAllByIdIn(List<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
                return List.of();
        }
        return uuids.stream()
                .map(binaryContentRepository::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    public void delete(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 binaryContent id 입니다."));
        binaryContentRepository.delete(binaryContent.getId());
    }
}
