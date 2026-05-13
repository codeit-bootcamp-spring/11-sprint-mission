package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BasicBinaryContentServiceTest {

  @InjectMocks
  private BasicBinaryContentService binaryContentService;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private BinaryContentMapper binaryContentMapper;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @Test
  @DisplayName("파일 업로드 성공")
  void create_success() {
    BinaryContentCreateRequest request = new BinaryContentCreateRequest(new byte[]{1, 2, 3},
        "image.png", 1024L,
        "image/png");
    BinaryContentDto dto = new BinaryContentDto(UUID.randomUUID(),
        "image.png", 1024L, "image/png");

    given(binaryContentMapper.toDto(any(BinaryContent.class))).willReturn(dto);

    BinaryContentDto result = binaryContentService.create(request);

    assertThat(result.fileName()).isEqualTo("image.png");
    then(binaryContentRepository).should().save(any(BinaryContent.class));
    then(binaryContentStorage).should().put(any(UUID.class), any(byte[].class));
  }

  @Test
  @DisplayName("파일 단건 조회 성공")
  void findById_success() {
    UUID fileId = UUID.randomUUID();
    BinaryContent content = new BinaryContent("image.png",
        1024L, "image/png");
    BinaryContentDto dto = new BinaryContentDto(fileId,
        "image.png", 1024L, "image/png");

    given(binaryContentRepository.findById(fileId)).willReturn(Optional.of(content));
    given(binaryContentMapper.toDto(content)).willReturn(dto);

    BinaryContentDto result = binaryContentService.findById(fileId);

    assertThat(result.id()).isEqualTo(fileId);
  }

  @Test
  @DisplayName("파일 단건 조회 실패 - 존재하지 않는 파일")
  void findById_fail_notFound() {
    UUID fileId = UUID.randomUUID();

    given(binaryContentRepository.findById(fileId)).willReturn(Optional.empty());

    assertThrows(BinaryContentNotFoundException.class, () -> {
      binaryContentService.findById(fileId);
    });
  }

  @Test
  @DisplayName("파일 삭제 성공")
  void delete_success() {
    UUID fileId = UUID.randomUUID();

    binaryContentService.delete(fileId);

    then(binaryContentRepository).should().deleteById(fileId);
    then(binaryContentStorage).should().delete(fileId);
  }
}