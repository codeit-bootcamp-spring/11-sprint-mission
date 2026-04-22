package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;

  @Override
  public BinaryContentDto findById(UUID id) {
    BinaryContent bc = binaryContentRepository.findById(id)
        .orElseThrow(
            () -> new IllegalArgumentException("BinaryContent with id " + id + " not found"));
    return toDto(bc);
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllByIdIn(ids).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    binaryContentRepository.deleteById(id);
  }

  private BinaryContentDto toDto(BinaryContent bc) {
    return new BinaryContentDto(
        bc.getId(),
        bc.getFileName(),
        bc.getSize(),
        bc.getContentType()
    );
  }
}