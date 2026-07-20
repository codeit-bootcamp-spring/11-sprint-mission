package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content; // 👉 import 추가
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext; // 👉 import 추가
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BinaryContentController.class)
@AutoConfigureMockMvc(addFilters = false)
class BinaryContentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BinaryContentService binaryContentService;

  @MockitoBean
  private BinaryContentStorage binaryContentStorage;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMappingContext;

  @Test
  @DisplayName("단건 바이너리 콘텐츠 조회 API 성공")
  void getBinaryContent_success() throws Exception {
    UUID id = UUID.randomUUID();
    BinaryContentDto dto = new BinaryContentDto(id, "test.png", 1024L, "image/png", "SUCCESS");

    given(binaryContentService.findById(id)).willReturn(dto);

    mockMvc.perform(get("/api/binaryContents/{binaryContentId}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fileName").value("test.png"))
        .andExpect(jsonPath("$.size").value(1024));
  }

  @Test
  @DisplayName("다중 바이너리 콘텐츠 조회 API 성공")
  void getMultipleBinaryContent_success() throws Exception {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    BinaryContentDto dto1 = new BinaryContentDto(id1, "test1.png", 1024L, "image/png", "SUCCESS");
    BinaryContentDto dto2 = new BinaryContentDto(id2, "test2.png", 2048L, "image/png", "SUCCESS");

    given(binaryContentService.findAllByIdIn(List.of(id1, id2))).willReturn(List.of(dto1, dto2));

    mockMvc.perform(get("/api/binaryContents")
            .param("binaryContentIds", id1.toString() + "," + id2.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].fileName").value("test1.png"))
        .andExpect(jsonPath("$[1].fileName").value("test2.png"));
  }

  @Test
  @DisplayName("바이너리 콘텐츠 다운로드 API 성공")
  void download_success() throws Exception {
    UUID id = UUID.randomUUID();
    BinaryContentDto dto = new BinaryContentDto(id, "test.png", 1024L, "image/png", "SUCCESS");
    byte[] fileContent = "dummy image data".getBytes();

    given(binaryContentService.findById(id)).willReturn(dto);
    given(binaryContentStorage.download(any(BinaryContentDto.class))).willReturn(
        (ResponseEntity) ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(fileContent)
    );

    mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG));
  }
}