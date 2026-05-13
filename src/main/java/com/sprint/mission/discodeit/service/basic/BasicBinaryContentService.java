package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.service.file.NonExistFileException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.JPABinaryContentRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final JPABinaryContentRepository binaryContentRepository;
  private final JPAUserRepository userRepository;
  private final JPAMessageRepository messageRepository;
  private final BinaryContentMapper binaryContentMapper;


  @Override
  @Transactional
  public BinaryContentDto create(BinaryContentCreateRequest binaryContentCreateRequest) {

    log.info("이진 콘텐츠 생성 요청, binaryContentCreateRequest : {}", binaryContentCreateRequest);

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

    log.info("이진 콘텐츠 생성 완료, content : {}", content);
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
    log.info("이진 콘텐츠 삭제 요청, binaryContentId : {}", binaryContentId);

    if (!binaryContentRepository.existsById(binaryContentId)) {
      throw new NonExistFileException(binaryContentId);
    }

    binaryContentRepository.deleteById(binaryContentId);
    log.info("이진 콘텐츠 삭제 완료, binaryContentId : {}", binaryContentId);

    return true;
  }


}
