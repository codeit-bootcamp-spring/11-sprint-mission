package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        request.fileName(), request.size(), request.contentType());
    
    binaryContentRepository.save(binaryContent);

    binaryContentStorage.put(binaryContent.getId(), request.bytes());

    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  public BinaryContentDto findById(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id).orElseThrow(
        () -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
    return binaryContentMapper.toDto(binaryContent);
  }

  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllByIdIn(ids).stream().
        map(binaryContentMapper::toDto).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    binaryContentRepository.deleteById(id);
  }
}
