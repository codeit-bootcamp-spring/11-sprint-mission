package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  private final BinaryContentMapper binaryContentMapper;

  @Override
  @Transactional
  public BinaryContentDto createBinaryContent(BinaryContentCreateRequest request) {
    if (request.bytes() == null || request.bytes().length == 0) {
      throw new BusinessLogicException(ExceptionCode.FILE_EMPTY);
    }

    long fileSize = request.bytes().length;

    BinaryContent content = new BinaryContent(request.fileName(),
        request.contentType(), fileSize);

    BinaryContent savedContent = binaryContentRepository.save(content);

    binaryContentStorage.put(savedContent.getId(), request.bytes());

    return binaryContentMapper.toDto(savedContent);
  }

  @Override
  @Transactional(readOnly = true)
  public BinaryContentDto find(UUID id) {
    BinaryContent content = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.FILE_NOT_FOUND));

    return binaryContentMapper.toDto(content);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BinaryContentDto> findAllByIdIn(Collection<UUID> ids) {
    if (ids == null) {
      return Collections.emptyList();
    }

    return binaryContentRepository.findAllById(ids)
        .stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void deleteBinaryContent(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.BINARY_CONTENT_NOT_EXISTS));
    binaryContentRepository.delete(binaryContent);
  }

  @Override
  @Transactional(readOnly = true)
  public ResponseEntity<Resource> download(UUID id) {
    BinaryContentDto dto = this.find(id);
    return binaryContentStorage.download(dto);
  }
}
