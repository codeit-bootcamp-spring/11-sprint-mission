package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.readstatus.DuplicateReadStatusException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReadStatusController.class)
@WithMockUser
class ReadStatusControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ReadStatusService readStatusService;

  private UUID readStatusId;
  private UUID userId;
  private UUID channelId;
  private Instant lastReadAt;
  private ReadStatusResponse readStatusResponse;

  @BeforeEach
  void setUp() {
    readStatusId = UUID.randomUUID();
    userId = UUID.randomUUID();
    channelId = UUID.randomUUID();
    lastReadAt = Instant.now();
    readStatusResponse = new ReadStatusResponse(readStatusId, userId, channelId, lastReadAt);
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("success")
    void create_success() throws Exception {
      // given
      ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, lastReadAt);

      given(readStatusService.createReadStatus(any())).willReturn(readStatusResponse);

      // when & then
      mockMvc.perform(post("/api/read-statuses")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(readStatusId.toString()))
          .andExpect(jsonPath("$.userId").value(userId.toString()))
          .andExpect(jsonPath("$.channelId").value(channelId.toString()));
    }

    @Test
    @DisplayName("fail with duplicate read status")
    void create_fail_duplicate_read_status_throws_exception() throws Exception {
      // given
      ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, lastReadAt);

      given(readStatusService.createReadStatus(any()))
          .willThrow(DuplicateReadStatusException.withUserAndChannel(userId, channelId));

      // when & then
      ErrorCode errorCode = ErrorCode.DUPLICATE_READ_STATUS;

      mockMvc.perform(post("/api/read-statuses")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.userId").value(userId.toString()))
          .andExpect(jsonPath("$.details.channelId").value(channelId.toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(DuplicateReadStatusException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("success")
    void update_success() throws Exception {
      // given
      Instant newLastReadAt = Instant.now();
      ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(newLastReadAt);
      ReadStatusResponse updatedResponse = new ReadStatusResponse(readStatusId, userId, channelId,
          newLastReadAt);

      given(readStatusService.updateReadStatus(eq(readStatusId), any()))
          .willReturn(updatedResponse);

      // when & then
      mockMvc.perform(patch("/api/read-statuses/{readStatusId}", readStatusId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(readStatusId.toString()))
          .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    @DisplayName("fail with read status not found")
    void update_fail_read_status_not_found_throws_exception() throws Exception {
      // given
      ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(Instant.now());

      given(readStatusService.updateReadStatus(eq(readStatusId), any()))
          .willThrow(ReadStatusNotFoundException.withId(readStatusId));

      // when & then
      ErrorCode errorCode = ErrorCode.READ_STATUS_NOT_FOUND;

      mockMvc.perform(patch("/api/read-statuses/{readStatusId}", readStatusId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.readStatusId").value(readStatusId.toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(ReadStatusNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("findAllByUserId")
  class FindAllByUserId {

    @Test
    @DisplayName("success")
    void findAllByUserId_success() throws Exception {
      // given
      List<ReadStatusResponse> responses = List.of(
          readStatusResponse,
          new ReadStatusResponse(UUID.randomUUID(), userId, UUID.randomUUID(), Instant.now())
      );
      given(readStatusService.findAllByUserId(userId)).willReturn(responses);

      // when & then
      mockMvc.perform(get("/api/read-statuses").param("userId", userId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].id").value(readStatusId.toString()))
          .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }
  }
}
