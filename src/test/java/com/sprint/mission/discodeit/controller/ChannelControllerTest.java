package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.DuplicateChannelException;
import com.sprint.mission.discodeit.exception.channel.NoValidParticipantsException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateForbiddenException;
import com.sprint.mission.discodeit.service.ChannelService;
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

@WebMvcTest(ChannelController.class)
@WithMockUser
class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ChannelService channelService;

  private UUID channelId;
  private UUID userId;
  private String channelName;
  private ChannelResponse publicChannelResponse;
  private ChannelResponse privateChannelResponse;

  @BeforeEach
  void setUp() {
    channelId = UUID.randomUUID();
    userId = UUID.randomUUID();
    channelName = "general";
    publicChannelResponse = new ChannelResponse(channelId, ChannelType.PUBLIC, channelName,
        "description", List.of(), Instant.now());
    privateChannelResponse = new ChannelResponse(channelId, ChannelType.PRIVATE, null, null,
        List.of(), null);
  }

  @Nested
  @DisplayName("createPublic")
  class CreatePublic {

    @Test
    @DisplayName("success")
    void createPublic_success() throws Exception {
      // given
      PublicChannelCreateRequest request = new PublicChannelCreateRequest(channelName,
          "description");

      given(channelService.createPublicChannel(any())).willReturn(publicChannelResponse);

      // when & then
      mockMvc.perform(post("/api/channels/public")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(channelId.toString()))
          .andExpect(jsonPath("$.type").value(ChannelType.PUBLIC.name()))
          .andExpect(jsonPath("$.name").value(channelName));
    }

    @Test
    @DisplayName("fail with duplicate channel")
    void createPublic_fail_duplicate_channel_throws_exception() throws Exception {
      // given
      PublicChannelCreateRequest request = new PublicChannelCreateRequest(channelName, null);

      given(channelService.createPublicChannel(any()))
          .willThrow(DuplicateChannelException.withName(channelName));

      // when & then
      ErrorCode errorCode = ErrorCode.DUPLICATE_CHANNEL;

      mockMvc.perform(post("/api/channels/public")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.name").value(channelName))
          .andExpect(
              jsonPath("$.exceptionType").value(DuplicateChannelException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("createPrivate")
  class CreatePrivate {

    @Test
    @DisplayName("success")
    void createPrivate_success() throws Exception {
      // given
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(userId));

      given(channelService.createPrivateChannel(any())).willReturn(privateChannelResponse);

      // when & then
      mockMvc.perform(post("/api/channels/private")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(channelId.toString()))
          .andExpect(jsonPath("$.type").value(ChannelType.PRIVATE.name()));
    }

    @Test
    @DisplayName("fail with no valid participants")
    void createPrivate_fail_no_valid_participants_throws_exception() throws Exception {
      // given
      UUID nonExistentUserId = UUID.randomUUID();
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
          List.of(nonExistentUserId));

      given(channelService.createPrivateChannel(any()))
          .willThrow(NoValidParticipantsException.withRequestedIds(List.of(nonExistentUserId)));

      // when & then
      ErrorCode errorCode = ErrorCode.NO_VALID_PARTICIPANTS;

      mockMvc.perform(post("/api/channels/private")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(
              jsonPath("$.exceptionType")
                  .value(NoValidParticipantsException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("success")
    void update_success() throws Exception {
      // given
      String newName = "updated";
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest(newName, null);
      ChannelResponse updatedResponse = new ChannelResponse(channelId, ChannelType.PUBLIC, newName,
          null, List.of(), null);

      given(channelService.updateChannel(eq(channelId), any())).willReturn(updatedResponse);

      // when & then
      mockMvc.perform(patch("/api/channels/{channelId}", channelId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(channelId.toString()))
          .andExpect(jsonPath("$.name").value(newName));
    }

    @Test
    @DisplayName("fail with private channel update forbidden")
    void update_fail_private_channel_update_forbidden_throws_exception() throws Exception {
      // given
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", null);

      given(channelService.updateChannel(eq(channelId), any()))
          .willThrow(PrivateChannelUpdateForbiddenException.withId(channelId));

      // when & then
      ErrorCode errorCode = ErrorCode.PRIVATE_CHANNEL_UPDATE_FORBIDDEN;

      mockMvc.perform(patch("/api/channels/{channelId}", channelId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.channelId").value(channelId.toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(PrivateChannelUpdateForbiddenException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("success")
    void delete_success() throws Exception {
      // given
      willDoNothing().given(channelService).deleteChannel(channelId);

      // when & then
      mockMvc.perform(delete("/api/channels/{channelId}", channelId)
              .with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("fail with channel not found")
    void delete_fail_channel_not_found_throws_exception() throws Exception {
      // given
      willThrow(ChannelNotFoundException.withId(channelId))
          .given(channelService).deleteChannel(channelId);

      // when & then
      ErrorCode errorCode = ErrorCode.CHANNEL_NOT_FOUND;

      mockMvc.perform(delete("/api/channels/{channelId}", channelId)
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.channelId").value(channelId.toString()))
          .andExpect(
              jsonPath("$.exceptionType").value(ChannelNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("findAllByUserId")
  class FindAllByUserId {

    @Test
    @DisplayName("success")
    void findAllByUserId_success() throws Exception {
      // given
      List<ChannelResponse> responses = List.of(publicChannelResponse, privateChannelResponse);
      given(channelService.findAllByUserId(userId)).willReturn(responses);

      // when & then
      mockMvc.perform(get("/api/channels").param("userId", userId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].id").value(channelId.toString()));
    }
  }
}
