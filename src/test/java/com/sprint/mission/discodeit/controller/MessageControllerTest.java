package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageService messageService;

  @Test
  void create_succeeds() throws Exception {
    UUID messageId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);
    UserDto authorDto = new UserDto(authorId, "testuser", "test@example.com", null, false);
    MessageDto messageDto = new MessageDto(messageId, Instant.now(), Instant.now(), "안녕하세요", channelId, authorDto, List.of());

    MockPart requestPart = new MockPart("messageCreateRequest", objectMapper.writeValueAsBytes(request));
    requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    given(messageService.create(any(), any())).willReturn(messageDto);

    mockMvc.perform(multipart("/api/messages").part(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(messageId.toString()))
        .andExpect(jsonPath("$.content").value("안녕하세요"))
        .andExpect(jsonPath("$.channelId").value(channelId.toString()));
  }

  @Test
  void create_withNonExistentChannel_returns404() throws Exception {
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);

    MockPart requestPart = new MockPart("messageCreateRequest", objectMapper.writeValueAsBytes(request));
    requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    given(messageService.create(any(), any())).willThrow(new ChannelNotFoundException(channelId));

    mockMvc.perform(multipart("/api/messages").part(requestPart))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void update_succeeds() throws Exception {
    UUID messageId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 메시지");
    UserDto authorDto = new UserDto(authorId, "testuser", "test@example.com", null, false);
    MessageDto updatedDto = new MessageDto(messageId, Instant.now(), Instant.now(), "수정된 메시지", channelId, authorDto, List.of());

    given(messageService.update(any(), any())).willReturn(updatedDto);

    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정된 메시지"));
  }

  @Test
  void update_withNonExistentMessage_returns404() throws Exception {
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 메시지");

    given(messageService.update(any(), any())).willThrow(new MessageNotFoundException(messageId));

    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void delete_succeeds() throws Exception {
    UUID messageId = UUID.randomUUID();

    willDoNothing().given(messageService).delete(messageId);

    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andExpect(status().isNoContent());
  }

  @Test
  void delete_withNonExistentMessage_returns404() throws Exception {
    UUID messageId = UUID.randomUUID();

    willThrow(new MessageNotFoundException(messageId)).given(messageService).delete(messageId);

    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
  }

  @Test
  void findAllByChannelId_succeeds() throws Exception {
    UUID channelId = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    UserDto authorDto = new UserDto(authorId, "testuser", "test@example.com", null, false);
    MessageDto messageDto = new MessageDto(messageId, Instant.now(), Instant.now(), "내용", channelId, authorDto, List.of());
    PageResponse<MessageDto> pageResponse = new PageResponse<>(List.of(messageDto), null, 50, false, null);

    given(messageService.findAllByChannelId(any(), any(), any())).willReturn(pageResponse);

    mockMvc.perform(get("/api/messages")
            .param("channelId", channelId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].content").value("내용"))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  @Test
  void findAllByChannelId_whenNoMessages_returnsEmptyList() throws Exception {
    UUID channelId = UUID.randomUUID();
    PageResponse<MessageDto> emptyResponse = new PageResponse<>(List.of(), null, 50, false, null);

    given(messageService.findAllByChannelId(any(), any(), any())).willReturn(emptyResponse);

    mockMvc.perform(get("/api/messages")
            .param("channelId", channelId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.hasNext").value(false));
  }
}