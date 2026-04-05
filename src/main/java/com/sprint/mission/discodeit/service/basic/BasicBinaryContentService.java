package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentInfoDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.CreateBinaryContentDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.CreateProfileImgDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final UserRepository userRepository;
  private final MessageRepository messageRepository;


  @Override
  public BinaryContentInfoDto create(CreateBinaryContentDto createBinaryContentDto) {
    BinaryContent content;
    try {
      content = new BinaryContent(

          createBinaryContentDto.userId(),
          createBinaryContentDto.messageId(),
          createBinaryContentDto.binaryFile().getOriginalFilename(),
          createBinaryContentDto.binaryFile().getContentType(),
          createBinaryContentDto.binaryFile().getBytes(),
          createBinaryContentDto.binaryFile().getSize()

      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    //존재 유저 체크
    if (!userRepository.isExistUser(createBinaryContentDto.userId())) {
      throw new NonExistException("존재하지 않는 유저 아이디입니다.");
    }
    //존재 메시지 체크
    if (!messageRepository.isExistMessage(createBinaryContentDto.messageId())) {
      throw new NonExistException("존재하지 않는 메시지 입니다.");
    }

    binaryContentRepository.saveBinaryContent(content);
    return contentToInfoDto(content);


  }

  @Override
  public BinaryContentInfoDto createProfileImg(CreateProfileImgDto createProfileImgDto) {
    BinaryContent content;
    try {
      content = new BinaryContent(
          createProfileImgDto.userId(),
          createProfileImgDto.file().getOriginalFilename(),
          createProfileImgDto.file().getContentType(),
          createProfileImgDto.file().getBytes(),
          createProfileImgDto.file().getSize()
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    //존재 유저 체크
    if (!userRepository.isExistUser(createProfileImgDto.userId())) {
      throw new NonExistException("존재하지 않는 유저 아이디입니다.");
    }

    binaryContentRepository.saveBinaryContent(content);
    return contentToInfoDto(content);
  }

  @Override
  public BinaryContentInfoDto find(UUID binaryContentId) {
    BinaryContent content = binaryContentRepository.getBinaryContent(binaryContentId).orElseThrow();
    return contentToInfoDto(content);
  }

  @Override
  public List<BinaryContentInfoDto> findAll() {
    return binaryContentRepository.getAllBinaryContent().stream()
        .map(this::contentToInfoDto)
        .toList();
  }

  @Override
  public List<BinaryContentInfoDto> findAllByUserId(UUID userId) {
    return binaryContentRepository.getAllByUserId(userId).stream()
        .map(this::contentToInfoDto)
        .toList();
  }


  @Override
  public boolean delete(UUID binaryContentId) {

    if (!binaryContentRepository.isExistBinaryContent(binaryContentId)) {
      throw new NonExistException("존재하지 않는 파일입니다.");
    }

    binaryContentRepository.deleteBinaryContent(binaryContentId);

    return true;
  }

  BinaryContentInfoDto contentToInfoDto(BinaryContent content) {

    return new BinaryContentInfoDto(

        content.getId(),
        content.getCreatedAt(),
        content.getFileName(),
        content.getSize(),
        content.getContentType(),
        content.getBytes()

    );


  }


}
