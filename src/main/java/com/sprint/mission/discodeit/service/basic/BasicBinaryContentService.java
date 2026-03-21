package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequestDto;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponseDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepo;

    @Override
    public BinaryContentResponseDto create(BinaryContentCreateRequestDto dto) {
        BinaryContent binaryContent = new BinaryContent(dto.fileName(), dto.data());
        binaryContentRepo.save(binaryContent);
        return toDto(binaryContent);
    }

    private BinaryContentResponseDto toDto(BinaryContent binaryContent) {
        return new BinaryContentResponseDto(
                binaryContent.getId(), binaryContent.getFileName(), binaryContent.getData());
    }

    @Override
    public BinaryContentResponseDto find(UUID id) {
        BinaryContent binaryContent = binaryContentRepo.findById(id)
                .orElseThrow(() -> new BinaryContentNotFoundException(id));

        return toDto(binaryContent);
    }

    @Override
    public List<BinaryContentResponseDto> findAllByIdIn(List<UUID> idList) {
        return idList.stream()
                .map(id -> binaryContentRepo.findById(id)
                        .orElseThrow(() -> new BinaryContentNotFoundException(id)))
                .map(this::toDto)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        BinaryContent binaryContent = binaryContentRepo.findById(id)
                .orElseThrow(() -> new BinaryContentNotFoundException(id));
        binaryContentRepo.delete(binaryContent);
    }
}
