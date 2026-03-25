package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContentDto create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = new BinaryContent(request.data());
        binaryContentRepository.save(binaryContent);
        return toDto(binaryContent);
    }

    @Override
    public BinaryContentDto findById(UUID id) {
        BinaryContent binaryContent = binaryContentRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
        return toDto(binaryContent);
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
        return binaryContentRepository.findAllByIdIn(ids).stream().
                map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.delete(id);
    }

    private BinaryContentDto toDto(BinaryContent binaryContent) {
        return new BinaryContentDto(binaryContent.getId(),
                binaryContent.getCreatedAt(), binaryContent.getData());
    }
}
