package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;

  @Override
  @Transactional
  public BinaryContent create(BinaryContentCreateRequest dto) {
    BinaryContent binaryContent = dto.toBinaryContent();
    binaryContentStorage.put(binaryContent.getId(), dto.bytes());
    binaryContentRepository.save(binaryContent);

    return binaryContent;
  }

  @Override
  @Transactional(readOnly = true)
  public BinaryContentDto find(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id).orElseThrow(
        () -> new DiscodeitException(ErrorCode.BINARY_CONTENT_NOT_FOUND)
    );
    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllById(ids).stream()
        .map(binaryContentMapper::toDto).toList();
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!binaryContentRepository.existsById(id)) {
      throw new DiscodeitException(ErrorCode.BINARY_CONTENT_NOT_FOUND);
    }
    binaryContentRepository.deleteById(id);
  }
}