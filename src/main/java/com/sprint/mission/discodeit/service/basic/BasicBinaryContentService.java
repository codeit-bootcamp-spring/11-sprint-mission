package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequestDto;
import com.sprint.mission.discodeit.dto.BinaryContentResponseDto;
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
    public BinaryContentResponseDto create(BinaryContentCreateRequestDto dto) {
        BinaryContent entity = new BinaryContent(
                "text.png", dto.getContent(),
                dto.getContentType(),
                dto.getUserId(),
                dto.getMessageId()
        );
        binaryContentRepository.save(entity);
        return toResponseDto(entity);
    }

    @Override
    public BinaryContentResponseDto findById(UUID id) {
        BinaryContent bc = binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("BinaryContent not found"));
        return toResponseDto(bc);
    }

    @Override
    public List<BinaryContentResponseDto> findAllByIdIn(List<UUID> ids) {
        return binaryContentRepository.findAllByIdIn(ids).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.deleteById(id);
    }

    private BinaryContentResponseDto toResponseDto(BinaryContent bc) {
        BinaryContentResponseDto dto = new BinaryContentResponseDto();
        dto.setId(bc.getId());
        dto.setContentType(bc.getContentType());
        dto.setUserId(bc.getUserId());
        dto.setMessageId(bc.getMessageId());
        return dto;
    }
}
