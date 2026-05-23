package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.common.PageResponse;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageWithoutChannelAccessException;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

  private UUID messageId;
  private UUID channelId;
  private UUID authorId;
  private String content;
  private MessageResponse messageResponse;

  @BeforeEach
  void setUp() {
    messageId = UUID.randomUUID();
    channelId = UUID.randomUUID();
    authorId = UUID.randomUUID();
    content = "hello";
    UserResponse author = new UserResponse(authorId, "tester", "tester@example.io", null, true);
    messageResponse = new MessageResponse(messageId, Instant.now(), Instant.now(), content,
        channelId, author, List.of());
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("success")
    void create_success() throws Exception {
      // given
      MessageCreateRequest request = new MessageCreateRequest(content, channelId, authorId);
      MockMultipartFile requestPart = new MockMultipartFile(
          "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      given(messageService.createMessage(any(), any())).willReturn(messageResponse);

      // when & then
      mockMvc.perform(multipart("/api/messages")
              .file(requestPart)
              .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(messageId.toString()))
          .andExpect(jsonPath("$.content").value(content))
          .andExpect(jsonPath("$.channelId").value(channelId.toString()));
    }

    @Test
    @DisplayName("fail with message without channel access")
    void create_fail_message_without_channel_access_throws_exception() throws Exception {
      // given
      MessageCreateRequest request = new MessageCreateRequest(content, channelId, authorId);
      MockMultipartFile requestPart = new MockMultipartFile(
          "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      given(messageService.createMessage(any(), any()))
          .willThrow(MessageWithoutChannelAccessException.withUserAndChannel(authorId, channelId));

      // when & then
      ErrorCode errorCode = ErrorCode.MESSAGE_WITHOUT_CHANNEL_ACCESS;

      mockMvc.perform(multipart("/api/messages")
              .file(requestPart)
              .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.userId").value(authorId.toString()))
          .andExpect(jsonPath("$.details.channelId").value(channelId.toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(MessageWithoutChannelAccessException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("success")
    void update_success() throws Exception {
      // given
      String newContent = "updated content";
      MessageUpdateRequest request = new MessageUpdateRequest(newContent);
      MockMultipartFile requestPart = new MockMultipartFile(
          "messageUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));
      MessageResponse updatedResponse = new MessageResponse(messageId, Instant.now(), Instant.now(),
          newContent, channelId, messageResponse.author(), List.of());

      given(messageService.updateMessage(eq(messageId), any(), any())).willReturn(updatedResponse);

      // when & then
      mockMvc.perform(multipart("/api/messages/{messageId}", messageId)
              .file(requestPart)
              .with(req -> {
                req.setMethod("PATCH");
                return req;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(messageId.toString()))
          .andExpect(jsonPath("$.content").value(newContent));
    }

    @Test
    @DisplayName("fail with message not found")
    void update_fail_message_not_found_throws_exception() throws Exception {
      // given
      MessageUpdateRequest request = new MessageUpdateRequest("updated");
      MockMultipartFile requestPart = new MockMultipartFile(
          "messageUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      given(messageService.updateMessage(eq(messageId), any(), any()))
          .willThrow(MessageNotFoundException.withId(messageId));

      // when & then
      ErrorCode errorCode = ErrorCode.MESSAGE_NOT_FOUND;

      mockMvc.perform(multipart("/api/messages/{messageId}", messageId)
              .file(requestPart)
              .with(req -> {
                req.setMethod("PATCH");
                return req;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.messageId").value(messageId.toString()))
          .andExpect(
              jsonPath("$.exceptionType").value(MessageNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("success")
    void delete_success() throws Exception {
      // given
      willDoNothing().given(messageService).deleteMessage(messageId);

      // when & then
      mockMvc.perform(delete("/api/messages/{messageId}", messageId))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("fail with message not found")
    void delete_fail_message_not_found_throws_exception() throws Exception {
      // given
      willThrow(MessageNotFoundException.withId(messageId))
          .given(messageService).deleteMessage(messageId);

      // when & then
      ErrorCode errorCode = ErrorCode.MESSAGE_NOT_FOUND;

      mockMvc.perform(delete("/api/messages/{messageId}", messageId))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.messageId").value(messageId.toString()))
          .andExpect(
              jsonPath("$.exceptionType").value(MessageNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("findAllByChannelId")
  class FindAllByChannelId {

    @Test
    @DisplayName("success")
    void findAllByChannelId_success() throws Exception {
      // given
      PageResponse<MessageResponse> pageResponse = new PageResponse<>(
          List.of(messageResponse), null, 1, false, 1L);
      given(messageService.findAllByChannelId(eq(channelId), any(), any()))
          .willReturn(pageResponse);

      // when & then
      mockMvc.perform(get("/api/messages").param("channelId", channelId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(1))
          .andExpect(jsonPath("$.content[0].id").value(messageId.toString()))
          .andExpect(jsonPath("$.hasNext").value(false))
          .andExpect(jsonPath("$.totalElements").value(1));
    }
  }
}
