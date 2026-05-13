package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicBinaryContentServiceTest {

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private BinaryContentMapper mapper;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @InjectMocks
  private BasicBinaryContentService binaryContentService;

  private UUID binaryContentId;
  private String fileName;
  private long size;
  private String contentType;
  private byte[] bytes;
  private BinaryContent binaryContent;
  private BinaryContentResponse response;

  @BeforeEach
  void setUp() {
    binaryContentId = UUID.randomUUID();
    fileName = UUID.randomUUID().toString();
    contentType = "image/png";
    bytes = "test".getBytes();
    size = bytes.length;
    binaryContent = new BinaryContent(fileName, size, contentType);
    ReflectionTestUtils.setField(binaryContent, "id", binaryContentId);
    response = new BinaryContentResponse(binaryContentId, fileName, size, contentType);
  }

  @Nested
  @DisplayName("create binary-content")
  class CreateBinaryContent {

    @Test
    @DisplayName("success")
    void createBinaryContent_success() {
      // given
      BinaryContentCreateRequest request = new BinaryContentCreateRequest(fileName, size,
          contentType, bytes);
      given(mapper.toResponse(any(BinaryContent.class))).willReturn(response);

      // when
      BinaryContentResponse result = binaryContentService.createBinaryContent(request);

      // then
      assertThat(result).isEqualTo(response);
      then(binaryContentRepository).should().save(any(BinaryContent.class));
      then(binaryContentStorage).should().put(any(UUID.class), any(byte[].class));
    }
  }

  @Nested
  @DisplayName("find by id")
  class FindById {

    @Test
    @DisplayName("success")
    void findById_success() {
      // given
      given(binaryContentRepository.findById(binaryContentId)).willReturn(
          Optional.of(binaryContent));
      given(mapper.toResponse(binaryContent)).willReturn(response);

      // when
      BinaryContentResponse result = binaryContentService.findById(binaryContentId);

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with binary-content not found")
    void findById_fail_not_found_throws_exception() {
      // given
      given(binaryContentRepository.findById(binaryContentId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> binaryContentService.findById(binaryContentId))
          .isInstanceOf(BinaryContentNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("find all by id in")
  class FindAllByIdIn {

    @Test
    @DisplayName("success with results")
    void findAllByIdIn_success() {
      // given
      List<UUID> ids = List.of(binaryContentId);
      given(binaryContentRepository.findAllByIdIn(ids)).willReturn(List.of(binaryContent));
      given(mapper.toResponse(binaryContent)).willReturn(response);

      // when
      List<BinaryContentResponse> result = binaryContentService.findAllByIdIn(ids);

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(response);
    }

    @Test
    @DisplayName("success with empty list")
    void findAllByIdIn_success_empty() {
      // given
      given(binaryContentRepository.findAllByIdIn(List.of())).willReturn(List.of());

      // when
      List<BinaryContentResponse> result = binaryContentService.findAllByIdIn(List.of());

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("delete binary-content")
  class DeleteBinaryContent {

    @Test
    @DisplayName("success")
    void deleteBinaryContent_success() {
      // given
      given(binaryContentRepository.findById(binaryContentId)).willReturn(
          Optional.of(binaryContent));

      // when
      binaryContentService.deleteBinaryContent(binaryContentId);

      // then
      then(binaryContentRepository).should().delete(binaryContent);
    }

    @Test
    @DisplayName("fail with binary-content not found")
    void deleteBinaryContent_fail_not_found_throws_exception() {
      // given
      given(binaryContentRepository.findById(binaryContentId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> binaryContentService.deleteBinaryContent(binaryContentId))
          .isInstanceOf(BinaryContentNotFoundException.class);
    }
  }
}