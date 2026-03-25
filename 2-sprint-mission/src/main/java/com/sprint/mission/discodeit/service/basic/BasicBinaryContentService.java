package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
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
    public BinaryContentDto.Response findById(UUID id) {
        return binaryContentRepository.findById(id)
                .map(BinaryContentDto.Response::of)
                .orElseThrow(() -> new NoSuchElementException("BinaryContent not found with id: " + id));
    }

    @Override
    public List<BinaryContentDto.Response> findAllByIdIn(List<UUID> ids) {
        List<BinaryContent> contents = binaryContentRepository.findAllByIdIn(ids);
        return contents.stream()
                .map(BinaryContentDto.Response::of)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        if (!binaryContentRepository.existsById(id)) {
            throw new NoSuchElementException("BinaryContent not found with id: " + id);
        }
        binaryContentRepository.deleteById(id);
    }
}