package com.sprint.mission.discodeit.control;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.advice.GlobalExceptionHandler;
import com.sprint.mission.discodeit.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@Import(GlobalExceptionHandler.class)
class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageService messageService;

  // --- POST ---
  @Test
  @DisplayName("유효한 요청으로 메시지 생성 시 201과 생성된 메시지 JSON을 반환")
  void createMessage_return201() throws Exception {
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    CreateMessageRequest request = new CreateMessageRequest(channelId, authorId, "test");
    MessageDto response = new MessageDto(UUID.randomUUID(), Instant.now(), Instant.now(), "test",
        channelId, null, List.of());
    given(messageService.createMessage(any(), any()))
        .willReturn(response);

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("test"))
        .andExpect(jsonPath("$.channelId").value(channelId.toString()));
  }

  @Test
  @DisplayName("내용이 비어있는 메시지 생성 요청 시 400과 VALIDATION_ERROR를 반환")
  void createMessage_blankContent_return400() throws Exception {
    CreateMessageRequest request = new CreateMessageRequest(
        UUID.randomUUID(), UUID.randomUUID(), " "
    );
    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  // --- GET ---
  @Test
  @DisplayName("channelId로 메시지 목록 조회 시 200과 함께 페이징 응답 JSON 반환")
  void readMessagesByChannel_success() throws Exception {
    UUID channelId = UUID.randomUUID();
    MessageDto msg1 = new MessageDto(UUID.randomUUID(), Instant.now(), Instant.now(), "Hi",
        channelId, null,
        List.of());
    MessageDto msg2 = new MessageDto(UUID.randomUUID(), Instant.now(), Instant.now(), "Hello",
        channelId, null,
        List.of());
    PageResponse<MessageDto> pageResponse = new PageResponse<>(List.of(msg1, msg2), null, 50, false,
        null);

    given(messageService.readMessagesByChannel(eq(channelId), isNull(), eq(50)))
        .willReturn(pageResponse);

    mockMvc.perform(get("/api/messages").param("channelId", channelId.toString()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].content").value("Hi"));
  }

  @Test
  @DisplayName("메시지가 없는 채널 조회 시 200과 함께 빈 content를 반환")
  void readMessageByChannel_empty_returnsEmptyContent() throws Exception {
    UUID channelId = UUID.randomUUID();
    PageResponse<MessageDto> pageResponse = new PageResponse<>(List.of(), null, 50, false, null);

    given(messageService.readMessagesByChannel(eq(channelId), isNull(), eq(50)))
        .willReturn(pageResponse);

    mockMvc.perform(get("/api/messages").param("channelId", channelId.toString()))
        .andDo(print())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  // --- DELETE ---
  @Test
  @DisplayName("존재하지 않는 메시지 삭제 시 204를 반환")
  void deleteMessage_existingMessage_return204() throws Exception {
    UUID messageId = UUID.randomUUID();
    willDoNothing().given(messageService).deleteMessage(messageId);

    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("존재하지 않는 메시지 삭제 시 404와 MESSAGE_NOT_FOUND를 반환")
  void deleteMessage_notFound_return404() throws Exception {
    UUID messageId = UUID.randomUUID();
    willThrow(new MessageNotFoundException(messageId))
        .given(messageService).deleteMessage(messageId);

    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
  }

  // --- PATCH ---
  @Test
  @DisplayName("유효한 내용으로 메시지 수정 시 200과 수정된 메시지 JSON을 반환")
  void updateMessage_return200() throws Exception {
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("newContent");
    MessageDto response = new MessageDto(messageId, Instant.now(), Instant.now(),
        "newContent", UUID.randomUUID(), null, List.of());
    given(messageService.updateMessage(eq(messageId), any()))
        .willReturn(response);

    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(jsonPath("$.content").value("newContent"))
        .andExpect(jsonPath("$.id").value(messageId.toString()));
  }

  @Test
  @DisplayName("내용이 비어있는 메시지 수정 요청 시 400과 VALIDATION_ERROR를 반환")
  void updateMessage_blankContent_return400() throws Exception {
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest(" ");

    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }
}