package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
@ActiveProfiles("test")
class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ChannelService channelService;

  // create(Public) 테스트
  @Test
  @DisplayName("올바른 퍼블릭 채널 정보로 생성 요청 - 201 Created 반환")
  void create_validPublicRequest_returnsCreatedChannel() throws Exception {
    // Given
    ChannelDto.CreatePublicRequest request = new ChannelDto.CreatePublicRequest(
        "test-channel",
        "채널 설명"
    );

    UUID channelId = UUID.randomUUID();
    ChannelDto.Response response = ChannelDto.Response.builder()
        .id(channelId)
        .type(ChannelType.PUBLIC)
        .name("test-channel")
        .description("채널 설명")
        .participants(new ArrayList<>())
        .lastMessageAt(Instant.now())
        .build();

    given(channelService.createPublicChannel(any(ChannelDto.CreatePublicRequest.class)))
        .willReturn(response);

    // When & Then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(channelId.toString()))
        .andExpect(jsonPath("$.type").value("PUBLIC"))
        .andExpect(jsonPath("$.name").value("test-channel"))
        .andExpect(jsonPath("$.description").value("채널 설명"));
  }

  @Test
  @DisplayName("유효하지 않은 정보로 퍼블릭 채널 생성 요청 - 400 Bad Request 반환")
  void create_invalidPublicRequest_returnsBadRequest() throws Exception {
    // Given
    ChannelDto.CreatePublicRequest invalidRequest = new ChannelDto.CreatePublicRequest(
        "",
        "설명"
    );

    // When & Then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  // create(Private) 테스트
  @Test
  @DisplayName("올바른 프라이빗 채널 정보로 생성 요청 - 201 Created 반환")
  void create_validPrivateRequest_returnsCreatedChannel() throws Exception {
    // Given
    List<UUID> participantIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    ChannelDto.CreatePrivateRequest request = new ChannelDto.CreatePrivateRequest(participantIds);

    UUID channelId = UUID.randomUUID();
    List<UserDto.Response> participants = new ArrayList<>();
    for (UUID userId : participantIds) {
      participants.add(UserDto.Response.builder()
          .id(userId)
          .username("user-" + userId.toString().substring(0, 5))
          .email("user" + userId.toString().substring(0, 5) + "@example.com")
          .profile(null)
          .online(false)
          .build());
    }

    ChannelDto.Response response = ChannelDto.Response.builder()
        .id(channelId)
        .type(ChannelType.PRIVATE)
        .name(null)
        .description(null)
        .participants(participants)
        .lastMessageAt(Instant.now())
        .build();

    given(channelService.createPrivateChannel(any(ChannelDto.CreatePrivateRequest.class)))
        .willReturn(response);

    // When & Then
    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(channelId.toString()))
        .andExpect(jsonPath("$.type").value("PRIVATE"))
        .andExpect(jsonPath("$.participants").isArray())
        .andExpect(jsonPath("$.participants.length()").value(2));
  }

  // update 테스트
  @Test
  @DisplayName("올바른 채널 정보로 수정 요청 - 200 OK 반환")
  void update_validRequest_returnsUpdatedChannel() throws Exception {
    // Given
    UUID channelId = UUID.randomUUID();
    ChannelDto.UpdateRequest request = new ChannelDto.UpdateRequest(
        "updated-channel",
        "updated 채널 설명"
    );

    ChannelDto.Response response = ChannelDto.Response.builder()
        .id(channelId)
        .type(ChannelType.PUBLIC)
        .name("updated-channel")
        .description("updated 채널 설명")
        .participants(new ArrayList<>())
        .lastMessageAt(Instant.now())
        .build();

    given(channelService.update(eq(channelId), any(ChannelDto.UpdateRequest.class)))
        .willReturn(response);

    // When & Then
    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(channelId.toString()))
        .andExpect(jsonPath("$.name").value("updated-channel"))
        .andExpect(jsonPath("$.description").value("updated 채널 설명"));
  }

  @Test
  @DisplayName("존재하지 않는 채널 수정 요청 - 404 Not Found 반환")
  void update_nonExistingChannel_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentChannelId = UUID.randomUUID();
    ChannelDto.UpdateRequest request = new ChannelDto.UpdateRequest("updated-channel", "설명");

    given(channelService.update(eq(nonExistentChannelId), any(ChannelDto.UpdateRequest.class)))
        .willThrow(ChannelNotFoundException.withId(nonExistentChannelId));

    // When & Then
    mockMvc.perform(patch("/api/channels/{channelId}", nonExistentChannelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("비공개 채널 수정 시도 요청 - 403 Forbidden 반환")
  void update_privateChannelUpdate_returnsForbidden() throws Exception {
    // Given
    UUID privateChannelId = UUID.randomUUID();
    ChannelDto.UpdateRequest request = new ChannelDto.UpdateRequest("updated-channel", "설명");

    given(channelService.update(eq(privateChannelId), any(ChannelDto.UpdateRequest.class)))
        .willThrow(PrivateChannelUpdateException.forChannel(privateChannelId));

    // When & Then
    mockMvc.perform(patch("/api/channels/{channelId}", privateChannelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  // delete 테스트
  @Test
  @DisplayName("채널 삭제 요청 - 204 No Content 반환")
  void delete_existingChannel_returnsNoContent() throws Exception {
    // Given
    UUID channelId = UUID.randomUUID();
    willDoNothing().given(channelService).delete(channelId);

    // When & Then
    mockMvc.perform(delete("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("존재하지 않는 채널 삭제 요청 - 404 Not Found 반환")
  void delete_nonExistingChannel_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentChannelId = UUID.randomUUID();
    willThrow(ChannelNotFoundException.withId(nonExistentChannelId))
        .given(channelService).delete(nonExistentChannelId);

    // When & Then
    mockMvc.perform(delete("/api/channels/{channelId}", nonExistentChannelId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  // findAllByUserId 테스트
  @Test
  @DisplayName("사용자 ID로 소속 채널 목록 조회 - 200 OK 반환")
  void findAllByUserId_validRequest_returnsChannelList() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    UUID channelId1 = UUID.randomUUID();
    UUID channelId2 = UUID.randomUUID();

    List<ChannelDto.Response> channels = List.of(
        ChannelDto.Response.builder()
            .id(channelId1)
            .type(ChannelType.PUBLIC)
            .name("public-channel")
            .description("공개 채널 설명")
            .participants(new ArrayList<>())
            .lastMessageAt(Instant.now())
            .build(),
        ChannelDto.Response.builder()
            .id(channelId2)
            .type(ChannelType.PRIVATE)
            .name(null)
            .description(null)
            .participants(List.of(
                UserDto.Response.builder().id(userId).username("user1").email("user1@example.com")
                    .online(true).build()))
            .lastMessageAt(Instant.now().minusSeconds(3600))
            .build()
    );

    given(channelService.findAllByUserId(userId)).willReturn(channels);

    // When & Then
    mockMvc.perform(get("/api/channels")
            .param("userId", userId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(channelId1.toString()))
        .andExpect(jsonPath("$[0].type").value("PUBLIC"))
        .andExpect(jsonPath("$[0].name").value("public-channel"))
        .andExpect(jsonPath("$[1].id").value(channelId2.toString()))
        .andExpect(jsonPath("$[1].type").value("PRIVATE"));
  }

  @Test
  @DisplayName("필수 파라미터 누락으로 소속 채널 목록 조회 - 400 Bad Request 반환")
  void findAllByUserId_missingUserId_returnsBadRequest() throws Exception {
    // Given

    // When & Then
    mockMvc.perform(get("/api/channels")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}