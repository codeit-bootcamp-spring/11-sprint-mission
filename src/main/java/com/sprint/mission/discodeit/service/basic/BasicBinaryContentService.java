package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepo;

    @Override
    @Transactional
    public BinaryContentDto create(BinaryContentCreateRequest dto) {
        BinaryContent binaryContent = new BinaryContent(dto.fileName(), dto.contentType(), dto.bytes());
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

        List<BinaryContent> binaryContents = binaryContentRepo.findAllById(idList);

        if(binaryContents.size() != idList.size()) {
            throw new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND);
        }

        return binaryContents.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        BinaryContent binaryContent = binaryContentRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND));
        binaryContentRepo.delete(binaryContent);
    }

    private BinaryContentDto toDto(BinaryContent binaryContent) {
        String bytes = Base64.getEncoder().encodeToString(binaryContent.getBytes());

        return new BinaryContentDto(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getFileName(),
                binaryContent.getBytes().length,
                binaryContent.getContentType(),
                bytes);
    }
}
