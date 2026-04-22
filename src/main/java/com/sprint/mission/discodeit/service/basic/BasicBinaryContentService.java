package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.JPABinaryContentRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final JPABinaryContentRepository binaryContentRepository;
  private final JPAUserRepository userRepository;
  private final JPAMessageRepository messageRepository;
  private final BinaryContentMapper binaryContentMapper;


  @Override
  @Transactional
  public BinaryContentDto create(BinaryContentCreateRequest binaryContentCreateRequest) {
    BinaryContent content;
    try {
      content = new BinaryContent(
          binaryContentCreateRequest.binaryFile().getOriginalFilename(),
          binaryContentCreateRequest.binaryFile().getContentType(),
          binaryContentCreateRequest.binaryFile().getSize()
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    binaryContentRepository.save(content);
    return binaryContentMapper.toDto(content);


  }

  @Override
  @Transactional(readOnly = true)
  public BinaryContentDto find(UUID binaryContentId) {
    BinaryContent content = binaryContentRepository.findById(binaryContentId)
        .orElseThrow();
    return binaryContentMapper.toDto(content);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BinaryContentDto> findAll() {
    return binaryContentRepository.findAll().stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }


  @Override
  @Transactional
  public boolean delete(UUID binaryContentId) {

    if (!binaryContentRepository.existsById(binaryContentId)) {
      throw new NonExistException("존재하지 않는 파일입니다.");
    }

    binaryContentRepository.deleteById(binaryContentId);

    return true;
  }


}
