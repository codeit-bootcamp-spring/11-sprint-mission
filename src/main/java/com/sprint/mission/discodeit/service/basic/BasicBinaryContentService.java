package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public BinaryContentDto create(BinaryContentCreateRequest request) {
        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                request.bytes() == null ? 0 : request.bytes().length,
                request.contentType()
        );

        BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);

        if (request.bytes() != null) {
            binaryContentStorage.put(savedBinaryContent.getId(), request.bytes());
        }

        return binaryContentMapper.toDto(savedBinaryContent);
    }

    @Override
    public Optional<BinaryContentDto> find(UUID id) {
        return binaryContentRepository.findById(id)
                .map(binaryContentMapper::toDto);
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
        return binaryContentRepository.findAllById(ids).stream()
                .map(binaryContentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        binaryContentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 BinaryContent가 존재하지 않습니다."));

        binaryContentRepository.deleteById(id);
    }

    @Override
    public Optional<BinaryContent> findEntity(UUID id) {
        return binaryContentRepository.findById(id);
    }
}