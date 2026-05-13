package com.sprint.mission.discodeit.controller;


import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channeldto.ChannelDto;
import com.sprint.mission.discodeit.dto.channeldto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ChannelService channelService;

  @Autowired
  private ObjectMapper objectMapper;


  @Test
  @DisplayName("공개 채널 생성")
  void createPublicChannelTest() throws Exception {

    PublicChannelCreateRequest request = new PublicChannelCreateRequest(
        "test channelname",
        "test channel description"
    );

    ChannelDto response = new ChannelDto(
        UUID.randomUUID(),
        ChannelType.PUBLIC,
        request.name(),
        request.description(),
        null,
        Instant.now()
    );

    given(channelService.createPublic(request)).willReturn(response);

    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("test channelname"))
        .andExpect(jsonPath("$.description").value("test channel description"))
        .andExpect(jsonPath("$.type").value("PUBLIC"));


  }

  @Test
  @DisplayName("개인 채널 생성")
  void createPrivateChannelTest() throws Exception {

    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
        List.of(UUID.randomUUID(), UUID.randomUUID())
    );

    ChannelDto response = new ChannelDto(
        UUID.randomUUID(),
        ChannelType.PRIVATE,
        null,
        null,
        List.of(new UserDto(request.participantIds().get(0),
                "test1",
                "test1@test.com",
                null,
                true
            ),
            new UserDto(request.participantIds().get(1),
                "test2",
                "test2@test.com",
                null,
                true
            )

        ),
        Instant.now()
    );

    given(channelService.createPrivate(request)).willReturn(response);

    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("PRIVATE"));


  }


}
