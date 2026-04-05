package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContentDto.Response create(BinaryContentDto.CreateRequest request) {
        BinaryContent binaryContent = request.toEntity();

        return BinaryContentDto.Response.of(binaryContentRepository.save(binaryContent));
    }

    @Override
    public BinaryContent findById(UUID id) {
        return binaryContentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND));
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return binaryContentRepository.findAllByIdIn(ids);
    }

    @Override
    public void delete(UUID id) {
        if (!binaryContentRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND);
        }
        binaryContentRepository.deleteById(id);
    }
}