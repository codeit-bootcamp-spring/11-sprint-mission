package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReadStatusController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ReadStatusControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @MockitoBean
  private ReadStatusService readStatusService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMappingContext;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  @DisplayName("읽음 상태 생성 성공 - 201 응답")
  void createReadStatus_success() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    Instant now = Instant.now();
    ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, now);
    ReadStatusDto response = new ReadStatusDto(UUID.randomUUID(), userId, channelId, now, true);

    given(readStatusService.create(any(ReadStatusCreateRequest.class))).willReturn(response);

    mockMvc.perform(post("/api/readStatuses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.notificationEnabled").value(true));
  }

  @Test
  @DisplayName("읽음 상태 수정 성공 - 200 응답")
  void updateReadStatus_success() throws Exception {
    UUID readStatusId = UUID.randomUUID();
    Instant newTime = Instant.now();
    ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(newTime, false);
    ReadStatusDto response = new ReadStatusDto(readStatusId, UUID.randomUUID(), UUID.randomUUID(),
        newTime, false);

    given(readStatusService.update(any(UUID.class), any(ReadStatusUpdateRequest.class)))
        .willReturn(response);

    mockMvc.perform(patch("/api/readStatuses/{readStatusId}", readStatusId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.notificationEnabled").value(false));
  }

  @Test
  @DisplayName("유저 ID로 읽음 상태 목록 조회 성공 - 200 응답")
  void getReadStatusByUserId_success() throws Exception {
    UUID userId = UUID.randomUUID();
    ReadStatusDto response = new ReadStatusDto(UUID.randomUUID(), userId, UUID.randomUUID(),
        Instant.now(), true);

    given(readStatusService.findAllByUserId(userId)).willReturn(List.of(response));

    mockMvc.perform(get("/api/readStatuses")
            .param("userId", userId.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].userId").value(userId.toString()));
  }
}