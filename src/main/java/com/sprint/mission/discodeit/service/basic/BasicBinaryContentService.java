package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
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
    public BinaryContentResponse createBinaryContent(BinaryContentCreateRequest binaryContentCreateRequest) {
        BinaryContent binaryContent = new BinaryContent(binaryContentCreateRequest);
        this.binaryContentRepository.save(binaryContent);

        log.info("BinaryContent has been created successfully. ✅ [ID: {}]", binaryContent.getId());
        return binaryContent.toResponse();
    }

    @Override
    public BinaryContentResponse findById(UUID id) {
        BinaryContent binaryContent = this.binaryContentRepository.findById(id);
        return binaryContent.toResponse();
    }

    @Override
    public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
        return this.binaryContentRepository.findAllByIdIn(ids).stream()
                .map(BinaryContent::toResponse)
                .toList();
    }

    @Override
    public void deleteBinaryContent(UUID id) {
        BinaryContent binaryContent = this.binaryContentRepository.findById(id);

        this.binaryContentRepository.delete(binaryContent);

        log.info("BinaryContent has been deleted successfully. ✅ [ID: {}]", binaryContent.getId());
    }
}
