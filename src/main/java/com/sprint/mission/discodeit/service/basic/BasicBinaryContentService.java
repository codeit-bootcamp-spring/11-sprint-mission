package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  private final BinaryContentMapper binaryContentMapper;

  @Override
  @Transactional
  public BinaryContentDto createBinaryContent(BinaryContentCreateRequest request) {
    log.debug("파일 업로드 비즈니스 로직 시작 - fileName: {}, contentType: {}", request.fileName(),
        request.contentType());
    if (request.bytes() == null || request.bytes().length == 0) {
      log.warn("파일 업로드 실패 - 빈 파일 - fileName: {}", request.fileName());
      throw new DiscodeitException(ErrorCode.FILE_EMPTY);
    }

    long fileSize = request.bytes().length;

    BinaryContent content = new BinaryContent(request.fileName(),
        request.contentType(), fileSize);

    BinaryContent savedContent = binaryContentRepository.save(content);

    binaryContentStorage.put(savedContent.getId(), request.bytes());

    log.info("파일 업로드 완료 - binaryContentId: {}, fileName: {}, size: {}bytes", savedContent.getId(),
        request.fileName(), fileSize);
    return binaryContentMapper.toDto(savedContent);
  }

  @Override
  @Transactional(readOnly = true)
  public BinaryContentDto find(UUID id) {
    BinaryContent content = binaryContentRepository.findById(id)
        .orElseThrow(() -> new DiscodeitException(ErrorCode.FILE_NOT_FOUND));

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
        .orElseThrow(() -> new DiscodeitException(ErrorCode.BINARY_CONTENT_NOT_EXISTS));
    binaryContentRepository.delete(binaryContent);
  }

  @Override
  @Transactional(readOnly = true)
  public ResponseEntity<Resource> download(UUID id) {
    log.debug("파일 다운로드 비즈니스 로직 시작 - binaryContentId: {}", id);
    BinaryContentDto dto = this.find(id);
    ResponseEntity<Resource> response = binaryContentStorage.download(dto);
    log.info("파일 다운로드 완료 - binaryContentId: {}, fileName: {}", id, dto.fileName());
    return response;
  }
}
