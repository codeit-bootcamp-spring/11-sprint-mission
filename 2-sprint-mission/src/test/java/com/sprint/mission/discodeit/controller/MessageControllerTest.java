package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.PageResponse;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@ActiveProfiles("test")
class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageService messageService;

  // create 테스트
  @Test
  @DisplayName("올바른 메시지 정보로 생성 요청 - 201 Created 반환")
  void create_validRequest_returnsCreatedMessage() throws Exception {
    // Given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageDto.CreateRequest createRequest = new MessageDto.CreateRequest(
        "test message",
        channelId,
        authorId
    );

    MockMultipartFile messageCreateRequestPart = new MockMultipartFile(
        "messageCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(createRequest)
    );

    MockMultipartFile attachment = new MockMultipartFile(
        "attachments",
        "test.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "test-image".getBytes()
    );

    UUID messageId = UUID.randomUUID();
    Instant now = Instant.now();

    UserDto.Response author = UserDto.Response.builder()
        .id(authorId)
        .username("testuser")
        .email("test@example.com")
        .profile(null)
        .online(true)
        .build();

    BinaryContentDto.Response attachmentDto = BinaryContentDto.Response.builder()
        .id(UUID.randomUUID())
        .fileName("test.jpg")
        .size(10L)
        .contentType(MediaType.IMAGE_JPEG_VALUE)
        .build();

    MessageDto.Response createdMessage = MessageDto.Response.builder()
        .id(messageId)
        .createdAt(now)
        .updatedAt(now)
        .content("test message")
        .channelId(channelId)
        .author(author)
        .attachments(List.of(attachmentDto))
        .build();

    given(messageService.create(any(MessageDto.CreateRequest.class), any()))
        .willReturn(createdMessage);

    // When & Then
    mockMvc.perform(multipart("/api/messages")
            .file(messageCreateRequestPart)
            .file(attachment)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(messageId.toString()))
        .andExpect(jsonPath("$.content").value("test message"))
        .andExpect(jsonPath("$.channelId").value(channelId.toString()))
        .andExpect(jsonPath("$.author.id").value(authorId.toString()))
        .andExpect(jsonPath("$.attachments[0].fileName").value("test.jpg"));
  }

  @Test
  @DisplayName("유효하지 않은 정보로 생성 요청 - 400 Bad Request 반환")
  void create_invalidRequest_returnsBadRequest() throws Exception {
    // Given
    MessageDto.CreateRequest invalidRequest = new MessageDto.CreateRequest(
        "",
        null,
        null
    );

    MockMultipartFile messageCreateRequestPart = new MockMultipartFile(
        "messageCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(invalidRequest)
    );

    // When & Then
    mockMvc.perform(multipart("/api/messages")
            .file(messageCreateRequestPart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("COMMON-002"))
        .andExpect(jsonPath("$.details.channelId").exists())
        .andExpect(jsonPath("$.details.authorId").exists());
  }

  // update 테스트
  @Test
  @DisplayName("올바른 메시지 정보로 수정 요청 - 200 OK 반환")
  void update_validRequest_returnsUpdatedMessage() throws Exception {
    // Given
    UUID messageId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();

    MessageDto.UpdateRequest updateRequest = new MessageDto.UpdateRequest(
        "updated message"
    );

    Instant now = Instant.now();

    UserDto.Response author = UserDto.Response.builder()
        .id(authorId)
        .username("testuser")
        .email("test@example.com")
        .profile(null)
        .online(true)
        .build();

    MessageDto.Response updatedMessage = MessageDto.Response.builder()
        .id(messageId)
        .createdAt(now.minusSeconds(60))
        .updatedAt(now)
        .content("updated message")
        .channelId(channelId)
        .author(author)
        .attachments(new ArrayList<>())
        .build();

    given(messageService.update(eq(messageId), any(MessageDto.UpdateRequest.class)))
        .willReturn(updatedMessage);

    // When & Then
    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(messageId.toString()))
        .andExpect(jsonPath("$.content").value("updated message"))
        .andExpect(jsonPath("$.channelId").value(channelId.toString()))
        .andExpect(jsonPath("$.author.id").value(authorId.toString()));
  }

  @Test
  @DisplayName("존재하지 않는 메시지 수정 요청 - 404 Not Found 반환")
  void update_nonExistingMessage_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentMessageId = UUID.randomUUID();

    MessageDto.UpdateRequest updateRequest = new MessageDto.UpdateRequest(
        "updated message"
    );

    given(messageService.update(eq(nonExistentMessageId), any(MessageDto.UpdateRequest.class)))
        .willThrow(MessageNotFoundException.withId(nonExistentMessageId));

    // When & Then
    mockMvc.perform(patch("/api/messages/{messageId}", nonExistentMessageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("MESSAGE-001"))
        .andExpect(jsonPath("$.message").value("존재하지 않는 메시지입니다."));
  }

  // delete 테스트
  @Test
  @DisplayName("메시지 삭제 요청 - 204 No Content 반환")
  void delete_existingMessage_returnsNoContent() throws Exception {
    // Given
    UUID messageId = UUID.randomUUID();
    willDoNothing().given(messageService).delete(messageId);

    // When & Then
    mockMvc.perform(delete("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("존재하지 않는 메시지 삭제 요청 - 404 Not Found 반환")
  void delete_nonExistingMessage_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentMessageId = UUID.randomUUID();
    willThrow(MessageNotFoundException.withId(nonExistentMessageId))
        .given(messageService).delete(nonExistentMessageId);

    // When & Then
    mockMvc.perform(delete("/api/messages/{messageId}", nonExistentMessageId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("MESSAGE-001"))
        .andExpect(jsonPath("$.message").value("존재하지 않는 메시지입니다."));
  }

  // findAllByChannelId 테스트
  @Test
  @DisplayName("채널 ID로 메시지 목록 조회 - 200 OK 반환")
  void findAllByChannelId_validRequest_returnsMessagePage() throws Exception {
    // Given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    Instant cursor = Instant.now();
    Pageable pageable = PageRequest.of(0, 50, Sort.Direction.DESC, "createdAt");

    UserDto.Response author = UserDto.Response.builder()
        .id(authorId)
        .username("testuser")
        .email("test@example.com")
        .profile(null)
        .online(true)
        .build();

    List<MessageDto.Response> messages = List.of(
        MessageDto.Response.builder()
            .id(UUID.randomUUID())
            .createdAt(cursor.minusSeconds(10))
            .updatedAt(cursor.minusSeconds(10))
            .content("첫 번째 메시지")
            .channelId(channelId)
            .author(author)
            .attachments(new ArrayList<>())
            .build(),
        MessageDto.Response.builder()
            .id(UUID.randomUUID())
            .createdAt(cursor.minusSeconds(20))
            .updatedAt(cursor.minusSeconds(20))
            .content("두 번째 메시지")
            .channelId(channelId)
            .author(author)
            .attachments(new ArrayList<>())
            .build()
    );

    PageResponse<MessageDto.Response> pageResponse = new PageResponse<>(
        messages,
        cursor.minusSeconds(30),
        pageable.getPageSize(),
        true,
        (long) messages.size()
    );

    given(messageService.findAllByChannelId(eq(channelId), eq(cursor), any(Pageable.class)))
        .willReturn(pageResponse);

    // When & Then
    mockMvc.perform(get("/api/messages")
            .param("channelId", channelId.toString())
            .param("cursor", cursor.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].content").value("첫 번째 메시지"))
        .andExpect(jsonPath("$.content[1].content").value("두 번째 메시지"))
        .andExpect(jsonPath("$.nextCursor").exists())
        .andExpect(jsonPath("$.size").value(50))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  @DisplayName("필수 파라미터 누락으로 메시지 목록 조회 - 400 Bad Request 반환")
  void findAllByChannelId_missingChannelId_returnsBadRequest() throws Exception {
    // Given

    // When & Then
    mockMvc.perform(get("/api/messages")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("COMMON-006"))
        .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 누락되었습니다."));
  }
}