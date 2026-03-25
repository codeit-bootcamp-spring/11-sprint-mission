package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContentDto create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                request.contentType(),
                request.bytes()
        );

        BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);

        return new BinaryContentDto(
                savedBinaryContent.getId(),
                savedBinaryContent.getFileName(),
                savedBinaryContent.getContentType(),
                savedBinaryContent.getBytes(),
                savedBinaryContent.getCreatedAt()
        );
    }

    @Override
    public Optional<BinaryContentDto> find(UUID id) {
        Optional<BinaryContent> optionalBinaryContent = binaryContentRepository.findById(id);

        if (optionalBinaryContent.isEmpty()) {
            return Optional.empty();
        }

        BinaryContent binaryContent = optionalBinaryContent.get();

        BinaryContentDto binaryContentDto = new BinaryContentDto(
                binaryContent.getId(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getBytes(),
                binaryContent.getCreatedAt()
        );

        return Optional.of(binaryContentDto);
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
        return ids.stream()
                .map(binaryContentRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(binaryContent -> new BinaryContentDto(
                        binaryContent.getId(),
                        binaryContent.getFileName(),
                        binaryContent.getContentType(),
                        binaryContent.getBytes(),
                        binaryContent.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 BinaryContent가 존재하지 않습니다."));

        binaryContentRepository.delete(id);
    }
}