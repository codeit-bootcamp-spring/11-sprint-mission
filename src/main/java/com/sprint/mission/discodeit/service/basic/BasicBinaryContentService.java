package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepo;

    @Override
    public BinaryContentDto create(BinaryContentCreateRequest dto) {
        BinaryContent binaryContent = new BinaryContent(dto.fileName(), dto.contentType(), dto.data());
        binaryContentRepo.save(binaryContent);
        return toDto(binaryContent);
    }

    @Override
    public BinaryContentDto find(UUID id) {
        BinaryContent binaryContent = binaryContentRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND));

        return toDto(binaryContent);
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> idList) {
        return idList.stream()
                .map(id -> binaryContentRepo.findById(id)
                        .orElseThrow(() -> new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND)))
                .map(this::toDto)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND));
        binaryContentRepo.deleteById(id);
    }

    private BinaryContentDto toDto(BinaryContent binaryContent) {
        String bytes = Base64.getEncoder().encodeToString(binaryContent.getData());

        return new BinaryContentDto(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getFileName(),
                binaryContent.getData().length,
                binaryContent.getContentType(),
                bytes);
    }
}
