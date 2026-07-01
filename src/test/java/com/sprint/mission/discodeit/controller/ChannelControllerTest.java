package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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

  @Test
  void 공개채널_생성_성공() throws Exception {
    UUID channelId = UUID.randomUUID();
    ChannelDto dto = new ChannelDto(channelId, "PUBLIC", "general", "공용 채널", List.of(), null);
    given(channelService.createPublic(any())).willReturn(dto);

    PublicChannelCreateRequest request = new PublicChannelCreateRequest("general", "공용 채널");

    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))  // 추가
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("general"))
        .andExpect(jsonPath("$.type").value("PUBLIC"));
  }

  @Test
  void 공개채널_생성_실패_이름없음() throws Exception {
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("", "공용 채널");

    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))  // 추가
        .andExpect(status().isBadRequest());
  }

  @Test
  void 채널_수정_성공() throws Exception {
    UUID channelId = UUID.randomUUID();
    ChannelDto dto = new ChannelDto(channelId, "PUBLIC", "newName", "새 설명", List.of(), null);
    given(channelService.update(any(), any())).willReturn(dto);

    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", "새 설명");

    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))  // 추가
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("newName"));
  }

  @Test
  void 채널_수정_실패_존재하지않는채널() throws Exception {
    UUID channelId = UUID.randomUUID();
    given(channelService.update(any(), any())).willThrow(new ChannelNotFoundException(channelId));

    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", "새 설명");

    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))  // 추가
        .andExpect(status().isNotFound());
  }

  @Test
  void 채널_삭제_성공() throws Exception {
    UUID channelId = UUID.randomUUID();
    willDoNothing().given(channelService).delete(channelId);

    mockMvc.perform(delete("/api/channels/{channelId}", channelId)
            .with(csrf()))  // 추가
        .andExpect(status().isNoContent());
  }

  @Test
  void 채널_목록_조회_성공() throws Exception {
    UUID userId = UUID.randomUUID();
    ChannelDto dto = new ChannelDto(UUID.randomUUID(), "PUBLIC", "general", "공용 채널", List.of(),
        null);
    given(channelService.findAllByUserId(any())).willReturn(List.of(dto));

    mockMvc.perform(get("/api/channels")
            .param("userId", userId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("general"));
  }
}