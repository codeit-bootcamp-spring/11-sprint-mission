package com.sprint.mission.discodeit.controller;


import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.messagedto.MessageDto;
import com.sprint.mission.discodeit.dto.messagedto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MessageService messageService;

  @Autowired
  private ObjectMapper objectMapper;


  @Test
  @DisplayName("메시지 생성")
  void postMessageTest() throws Exception {

    MessageCreateRequest request = new MessageCreateRequest(
        "test message",
        UUID.randomUUID(),
        UUID.randomUUID()
    );

    MessageDto response = new MessageDto(
        UUID.randomUUID(),
        Instant.now(),
        Instant.now(),
        request.content(),
        request.channelId(),
        new UserDto(
            request.authorId(),
            "testuser",
            "test1@test.com",
            null,
            true

        ),
        null
    );

    MockMultipartFile contentPart = new MockMultipartFile(
        "messageCreateRequest",
        null,
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    given(messageService.create(request, null)).willReturn(response);

    mockMvc.perform(multipart("/api/messages")
            .file(contentPart)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("test message"));


  }

  @Test
  @DisplayName("메시지 찾기")
  void getMessageTest() throws Exception {

    UUID channelId = UUID.randomUUID();

    PageResponse<MessageDto> response = new PageResponse<>(

        List.of(new MessageDto(
                UUID.randomUUID(),
                Instant.now(),
                Instant.now(),
                "testmessage1",
                channelId,
                new UserDto(
                    UUID.randomUUID(),
                    "testuser",
                    "test1@test.com",
                    null,
                    true
                ),
                null

            ),
            new MessageDto(
                UUID.randomUUID(),
                Instant.now(),
                Instant.now(),
                "testmessage2",
                channelId,
                new UserDto(
                    UUID.randomUUID(),
                    "testuser2",
                    "test2@test.com",
                    null,
                    true
                ),
                null
            )),
        null,
        10,
        false,
        2L

    );

    Pageable pageable = PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"));

    given(messageService.findAllByChannelId(channelId, pageable, null)).willReturn(response);

    mockMvc.perform(get("/api/messages")
            .param("channelId", channelId.toString())
            .param("page", "0")
            .param("size", "10")
            .param("sort", "createdAt,desc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].content").value("testmessage1"));


  }


}
