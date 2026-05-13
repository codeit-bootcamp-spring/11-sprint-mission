package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
  void 메시지_생성_성공() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    UUID messageId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);
    MessageDto dto = new MessageDto(messageId, Instant.now(), Instant.now(), "안녕하세요", channelId,
        null, List.of());
    given(messageService.create(any(), any())).willReturn(dto);

    MockMultipartFile messagePart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));

    // when & then
    mockMvc.perform(multipart("/api/messages")
            .file(messagePart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("안녕하세요"));
  }

  @Test
  void 메시지_생성_실패_채널ID없음() throws Exception {
    // given
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", null, UUID.randomUUID());

    MockMultipartFile messagePart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));

    // when & then
    mockMvc.perform(multipart("/api/messages")
            .file(messagePart))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 메시지_목록_조회_성공() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    MessageDto dto = new MessageDto(UUID.randomUUID(), Instant.now(), Instant.now(), "안녕하세요",
        channelId, null, List.of());
    PageResponse<MessageDto> pageResponse = new PageResponse<>(List.of(dto), null, 50, false, null);
    given(messageService.findAllByChannelId(any(), any(), any(Integer.class))).willReturn(
        pageResponse);

    // when & then
    mockMvc.perform(get("/api/messages")
            .param("channelId", channelId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].content").value("안녕하세요"));
  }

  @Test
  void 메시지_수정_성공() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");
    MessageDto dto = new MessageDto(messageId, Instant.now(), Instant.now(), "수정된 내용", channelId,
        null, List.of());
    given(messageService.update(any(), any())).willReturn(dto);

    // when & then
    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정된 내용"));
  }

  @Test
  void 메시지_수정_실패_존재하지않는메시지() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");
    given(messageService.update(any(), any())).willThrow(new MessageNotFoundException(messageId));

    // when & then
    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void 메시지_삭제_성공() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();
    willDoNothing().given(messageService).delete(messageId);

    // when & then
    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andExpect(status().isNoContent());
  }
}