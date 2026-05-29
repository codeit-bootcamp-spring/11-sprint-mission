package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.storage.DownloadResult;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BinaryContentController.class)
class BinaryContentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BinaryContentService binaryContentService;

  @MockitoBean
  private BinaryContentStorage binaryContentStorage;

  private UUID binaryContentId;
  private String fileName;
  private String contentType;
  private BinaryContentResponse binaryContentResponse;

  @BeforeEach
  void setUp() {
    binaryContentId = UUID.randomUUID();
    fileName = "image.png";
    contentType = "image/png";
    binaryContentResponse = new BinaryContentResponse(binaryContentId, fileName, 1024L,
        contentType);
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("success")
    void findById_success() throws Exception {
      // given
      given(binaryContentService.findById(binaryContentId)).willReturn(binaryContentResponse);

      // when & then
      mockMvc.perform(get("/api/binary-contents/{binaryContentId}", binaryContentId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(binaryContentId.toString()))
          .andExpect(jsonPath("$.fileName").value(fileName))
          .andExpect(jsonPath("$.contentType").value(contentType))
          .andExpect(jsonPath("$.size").value(1024));
    }

    @Test
    @DisplayName("fail with binary content not found")
    void findById_fail_binary_content_not_found_throws_exception() throws Exception {
      // given
      given(binaryContentService.findById(binaryContentId))
          .willThrow(BinaryContentNotFoundException.withId(binaryContentId));

      // when & then
      ErrorCode errorCode = ErrorCode.BINARY_CONTENT_NOT_FOUND;

      mockMvc.perform(get("/api/binary-contents/{binaryContentId}", binaryContentId))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.binaryContentId").value(binaryContentId.toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(BinaryContentNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("findAllByIdIn")
  class FindAllByIdIn {

    @Test
    @DisplayName("success")
    void findAllByIdIn_success() throws Exception {
      // given
      UUID anotherId = UUID.randomUUID();
      BinaryContentResponse anotherResponse = new BinaryContentResponse(anotherId, "doc.pdf",
          2048L, "application/pdf");
      List<BinaryContentResponse> responses = List.of(binaryContentResponse, anotherResponse);

      given(binaryContentService.findAllByIdIn(any())).willReturn(responses);

      // when & then
      mockMvc.perform(get("/api/binary-contents")
              .param("binaryContentIds", binaryContentId.toString(), anotherId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].id").value(binaryContentId.toString()))
          .andExpect(jsonPath("$[1].id").value(anotherId.toString()));
    }
  }

  @Nested
  @DisplayName("download")
  class Download {

    @Test
    @DisplayName("success")
    void download_success() throws Exception {
      // given
      byte[] fileBytes = "fake image content".getBytes();
      DownloadResult.Stream streamResult = new DownloadResult.Stream(
          new ByteArrayResource(fileBytes), fileName, contentType, (long) fileBytes.length
      );

      given(binaryContentService.findById(binaryContentId)).willReturn(binaryContentResponse);
      given(binaryContentStorage.download(binaryContentResponse)).willReturn(streamResult);

      // when & then
      mockMvc.perform(
              get("/api/binary-contents/{binaryContentId}/download", binaryContentId))
          .andExpect(status().isOk())
          .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + fileName + "\""))
          .andExpect(content().contentType(contentType))
          .andExpect(content().bytes(fileBytes));
    }

    @Test
    @DisplayName("fail with binary content not found")
    void download_fail_binary_content_not_found_throws_exception() throws Exception {
      // given
      given(binaryContentService.findById(binaryContentId))
          .willThrow(BinaryContentNotFoundException.withId(binaryContentId));

      // when & then
      ErrorCode errorCode = ErrorCode.BINARY_CONTENT_NOT_FOUND;

      mockMvc.perform(
              get("/api/binary-contents/{binaryContentId}/download", binaryContentId))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.binaryContentId").value(binaryContentId.toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(BinaryContentNotFoundException.class.getSimpleName()));
    }
  }
}
