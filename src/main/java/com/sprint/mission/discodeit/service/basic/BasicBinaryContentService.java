package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContentResponse createBinaryContent(BinaryContentCreateRequest req) {
        BinaryContent binaryContent = new BinaryContent(req.data(), req.fileName(), req.contentType(), req.size());
        this.binaryContentRepository.save(binaryContent);

        log.info("binary content has been created successfully. ✅ [ID: {}]", binaryContent.getId());
        return this.toResponse(binaryContent);
    }

    @Override
    public BinaryContentResponse findById(UUID id) {
        return this.toResponse(this.binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested binary content not found. ❌")));
    }

    @Override
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
        return this.binaryContentRepository.findAllByIdIn(ids).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteBinaryContent(UUID id) {
        BinaryContent binaryContent = this.binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("requested binary content not found. ❌"));

        this.binaryContentRepository.delete(binaryContent);

        log.info("binary content has been deleted successfully. ✅ [ID: {}]", binaryContent.getId());
    }

    private BinaryContentResponse toResponse(BinaryContent binaryContent) {
        return new BinaryContentResponse(
                binaryContent.getData(),
                binaryContent.getFileName(),
                binaryContent.getContentType(),
                binaryContent.getSize()
        );
    }
}
