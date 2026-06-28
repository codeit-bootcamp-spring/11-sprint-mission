package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@WithMockUser
class BinaryContentApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BinaryContentService binaryContentService;

  private UUID binaryContentId;
  private String fileName;
  private String contentType;
  private byte[] bytes;

  @BeforeEach
  void setUp() {
    fileName = "image.png";
    contentType = "image/png";
    bytes = "fake image content".getBytes();

    BinaryContentResponse created = binaryContentService.createBinaryContent(
        new BinaryContentCreateRequest(fileName, (long) bytes.length, contentType, bytes)
    );
    binaryContentId = created.id();
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("success")
    void findById_success() throws Exception {
      // when & then
      mockMvc.perform(get("/api/binary-contents/{binaryContentId}", binaryContentId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(binaryContentId.toString()))
          .andExpect(jsonPath("$.fileName").value(fileName))
          .andExpect(jsonPath("$.contentType").value(contentType))
          .andExpect(jsonPath("$.size").value(bytes.length));
    }

    @Test
    @DisplayName("fail with binary content not found")
    void findById_fail_binary_content_not_found_throws_exception() throws Exception {
      // given
      UUID nonExistentId = UUID.randomUUID();

      // when & then
      ErrorCode errorCode = ErrorCode.BINARY_CONTENT_NOT_FOUND;

      mockMvc.perform(get("/api/binary-contents/{binaryContentId}", nonExistentId))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.binaryContentId").value(nonExistentId.toString()))
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
      // when & then
      mockMvc.perform(get("/api/binary-contents")
              .param("binaryContentIds", binaryContentId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(1))
          .andExpect(jsonPath("$[0].id").value(binaryContentId.toString()))
          .andExpect(jsonPath("$[0].fileName").value(fileName));
    }
  }

  @Nested
  @DisplayName("download")
  class Download {

    @Test
    @DisplayName("success")
    void download_success() throws Exception {
      // when & then
      mockMvc.perform(get("/api/binary-contents/{binaryContentId}/download", binaryContentId))
          .andExpect(status().isOk())
          .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + fileName + "\""))
          .andExpect(content().contentType(contentType))
          .andExpect(content().bytes(bytes));
    }

    @Test
    @DisplayName("fail with binary content not found")
    void download_fail_binary_content_not_found_throws_exception() throws Exception {
      // given
      UUID nonExistentId = UUID.randomUUID();

      // when & then
      ErrorCode errorCode = ErrorCode.BINARY_CONTENT_NOT_FOUND;

      mockMvc.perform(get("/api/binary-contents/{binaryContentId}/download", nonExistentId))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.binaryContentId").value(nonExistentId.toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(BinaryContentNotFoundException.class.getSimpleName()));
    }
  }
}