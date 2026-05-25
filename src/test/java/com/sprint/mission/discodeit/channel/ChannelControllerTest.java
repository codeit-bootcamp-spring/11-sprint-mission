package com.sprint.mission.discodeit.channel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.controller.ChannelController;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;
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
public class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ChannelService channelService;

  @Test
  @DisplayName("퍼블릭 채널 생성 성공 - 201 Created 응답 반환")
  void createPublic_success() throws Exception {
    //given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("채팅방", "채팅방");
    ChannelDto dto = new ChannelDto(UUID.randomUUID(), "채팅방", "채팅방", ChannelType.PUBLIC, null,
        null);

    given(channelService.createPublicChannel(any(PublicChannelCreateRequest.class))).willReturn(
        dto);

    //when & then
    mockMvc.perform(
            post("/api/channels/public")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print())
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("프라이빗 채널 생성 성공 - 201 Created 응답 반환")
  void createPrivate_success() throws Exception {
    //given
    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
        List.of(UUID.randomUUID(), UUID.randomUUID()));
    ChannelDto dto = new ChannelDto(UUID.randomUUID(), "채팅방", "채팅방", ChannelType.PUBLIC, null,
        null);

    given(channelService.createPrivateChannel(any(PrivateChannelCreateRequest.class))).willReturn(
        dto);

    //when & then
    mockMvc.perform(
            post("/api/channels/private")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andDo(print())
        .andExpect(status().isCreated());
  }
}
