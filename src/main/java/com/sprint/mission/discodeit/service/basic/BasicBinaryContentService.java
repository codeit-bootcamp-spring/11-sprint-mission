package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
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
                .orElseThrow(() -> new BinaryContentNotFoundException(id));

        return toDto(binaryContent);
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> idList) {
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

    private BinaryContentDto toDto(BinaryContent binaryContent) {
        String base64 = Base64.getEncoder().encodeToString(binaryContent.getData());

        return new BinaryContentDto(
                binaryContent.getId(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                base64);
    }
}
