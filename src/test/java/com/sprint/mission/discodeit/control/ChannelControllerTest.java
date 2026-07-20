package com.sprint.mission.discodeit.control;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.advice.GlobalExceptionHandler;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ChannelService channelService;

  // --- POST(PUBLIC) ---
  @Test
  @DisplayName("유효한 요청으로 Public 채널 생성 시 201과 채널 JSON을 반환")
  void createPublicChannel_returns201WithBody() throws Exception {
    PublicChannelRequest request = new PublicChannelRequest("test", "test channel");
    ChannelDto response = new ChannelDto(
        UUID.randomUUID(), "test", ChannelType.PUBLIC, "test channel", Instant.now(), List.of()
    );
    given(channelService.createPublicChannel(any())).willReturn(response);

    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("test"))
        .andExpect(jsonPath("$.type").value("PUBLIC"));
  }

  @Test
  @DisplayName("채널명이 1자일 경우 Public 채널 생성 시 400을 반환")
  void createPublicChannel_shortName_returns400() throws Exception {
    PublicChannelRequest request = new PublicChannelRequest("t", "test");

    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  // --- POST(PRIVATE) ---
  @Test
  @DisplayName("유효한 요청으로 Private 채널 생성 시 201과 채널 JSON을 반환")
  void createPrivateChannel_returns201() throws Exception {
    PrivateChannelRequest request = new PrivateChannelRequest(List.of(UUID.randomUUID()));
    ChannelDto response = new ChannelDto(
        UUID.randomUUID(), "Private Channel", ChannelType.PRIVATE, "", Instant.now(), List.of()
    );
    given(channelService.createPrivateChannel(any())).willReturn(response);

    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("PRIVATE"));
  }

  @Test
  @DisplayName("참가자 목록이 비어있으면 400을 반환")
  void createPrivateChannel_emptyParticipants_returns400() throws Exception {
    PrivateChannelRequest request = new PrivateChannelRequest(List.of());

    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  // --- GET ---
  @Test
  @DisplayName("userId로 채널 목록 조회 시 200과 채널 목록 JSON을 반환")
  void readAllByUserId_success() throws Exception {
    UUID userId = UUID.randomUUID();
    ChannelDto ch1 = new ChannelDto(UUID.randomUUID(), "test1", ChannelType.PUBLIC, "",
        Instant.now(), List.of());
    ChannelDto ch2 = new ChannelDto(UUID.randomUUID(), "test2", ChannelType.PUBLIC, "",
        Instant.now(), List.of());
    given(channelService.findAllByUserId(userId)).willReturn(List.of(ch1, ch2));

    mockMvc.perform(get("/api/channels").param("userId", userId.toString()))
        .andDo(print())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("test1"));
  }

  // --- DELETE ---
  @Test
  @DisplayName("존재하는 채널 삭제 시 204를 반환")
  void deleteChannel_returns204() throws Exception {
    UUID channelId = UUID.randomUUID();
    willDoNothing().given(channelService).deleteChannel(channelId);

    mockMvc.perform(delete("/api/channels/{channelId}", channelId))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("존재하지 않는 채널 삭제 시 404와 CHANNEL_NOT_FOUND를 반환")
  void deleteChannel_notFound_returns404() throws Exception {
    UUID channelId = UUID.randomUUID();
    willThrow(new ChannelNotFoundException(channelId)).given(channelService)
        .deleteChannel(channelId);

    mockMvc.perform(delete("/api/channels/{channelId}", channelId))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
  }

  // --- PATCH ---
  @Test
  @DisplayName("Public 채널 수정 시 200과 수정된 채널 JSON을 반환")
  void updateChannel_publicChannel_returns200() throws Exception {
    UUID channelId = UUID.randomUUID();
    ChannelUpdateRequest request = new ChannelUpdateRequest("newName", "newDesc");
    ChannelDto response = new ChannelDto(channelId, "newName", ChannelType.PUBLIC, "newDesc",
        Instant.now(), List.of());
    given(channelService.updateChannel(eq(channelId), any())).willReturn(response);

    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("newName"));
  }

  @Test
  @DisplayName("Private 채널 수정 시도 시 400과 CHANNEL_MODIFY_PRIVATE을 반환")
  void updateChannel_privateChannel_returns400() throws Exception {
    UUID channelId = UUID.randomUUID();
    ChannelUpdateRequest request = new ChannelUpdateRequest("newName", null);
    willThrow(new PrivateChannelUpdateException(channelId))
        .given(channelService).updateChannel(eq(channelId), any());

    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CHANNEL_MODIFY_PRIVATE"));
  }
}