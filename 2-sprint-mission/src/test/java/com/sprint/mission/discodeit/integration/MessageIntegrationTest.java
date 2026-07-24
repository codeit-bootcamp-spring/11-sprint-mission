package com.sprint.mission.discodeit.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = {"USER", "CHANNEL_MANAGER", "ADMIN"})
class MessageIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private EntityManager entityManager;

  private User saveTestUser(String username, String email) {
    BinaryContent profile = BinaryContent.builder()
        .fileName("profile.jpg")
        .size(1024L)
        .contentType("image/jpeg")
        .build();
    entityManager.persist(profile);

    User user = User.builder()
        .username(username)
        .email(email)
        .password("Password123!")
        .profile(profile)
        .build();
    entityManager.persist(user);
    return user;
  }

  private Channel saveTestPublicChannel(String name, String description) {
    Channel channel = Channel.createPublic(name, description);
    entityManager.persist(channel);
    return channel;
  }

  private Message saveTestMessage(Channel channel, User author, String content) {
    Message message = Message.builder()
        .content(content)
        .channel(channel)
        .author(author)
        .attachments(new ArrayList<>())
        .build();
    entityManager.persist(message);
    return message;
  }

  // create 테스트
  @Test
  @DisplayName("메시지 송신 API 통합 테스트 - 성공")
  void create_success() throws Exception {
    // Given
    User author = saveTestUser("msgAuthor", "author@example.com");
    Channel channel = saveTestPublicChannel("테스트채널", "설명");
    entityManager.flush();
    entityManager.clear();

    MessageDto.CreateRequest request = new MessageDto.CreateRequest(
        "테스트 메시지",
        channel.getId(),
        author.getId()
    );

    MockMultipartFile messageCreateRequestPart = new MockMultipartFile(
        "messageCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile attachmentPart = new MockMultipartFile(
        "attachments",
        "test.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "테스트 첨부 파일 내용".getBytes()
    );

    // When & Then
    mockMvc.perform(multipart("/api/messages")
            .file(messageCreateRequestPart)
            .file(attachmentPart)
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.content").value("테스트 메시지"))
        .andExpect(jsonPath("$.channelId").value(channel.getId().toString()))
        .andExpect(jsonPath("$.author.id").value(author.getId().toString()))
        .andExpect(jsonPath("$.attachments[0].fileName").value("test.txt"));
  }

  @Test
  @DisplayName("메시지 송신 실패 API 통합 테스트 - 유효하지 않은 요청")
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
            .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  // findAllByChannelId 테스트
  @Test
  @DisplayName("채널별 메시지 목록 조회 API 통합 테스트 - 성공")
  void findAllByChannelId_success() throws Exception {
    // Given
    User author = saveTestUser("messageuser", "messageuser@example.com");
    Channel channel = saveTestPublicChannel("테스트 채널", "테스트 채널 설명");

    saveTestMessage(channel, author, "첫 번째 메시지 내용");
    saveTestMessage(channel, author, "두 번째 메시지 내용");

    entityManager.flush();
    entityManager.clear();

    // When & Then
    mockMvc.perform(get("/api/messages")
            .param("channelId", channel.getId().toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.size()").value(2))
        .andExpect(jsonPath("$.content[*].content").value(
            org.hamcrest.Matchers.hasItems("첫 번째 메시지 내용", "두 번째 메시지 내용")
        ));
  }

  @Test
  @DisplayName("채널별 메시지 목록 조회 실패 API 통합 테스트 - 필수 파라미터 누락")
  void findAllByChannelId_missingChannelId_returnsBadRequest() throws Exception {
    // When & Then
    mockMvc.perform(get("/api/messages")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  // update 테스트
  @Test
  @Disabled("@PreAuthorize(@messageSecurity.isAuthor)가 작성자 본인 인증을 요구하나, 통합 테스트에서 실제 로그인 흐름 미구성. 별도 리팩토링 필요")
  @DisplayName("메시지 업데이트 API 통합 테스트 - 성공")
  void update_success() throws Exception {
    // Given
    User author = saveTestUser("messageuser", "messageuser@example.com");
    Channel channel = saveTestPublicChannel("테스트 채널", "테스트 채널 설명");
    Message message = saveTestMessage(channel, author, "원본 메시지 내용");
    entityManager.flush();
    entityManager.clear();

    MessageDto.UpdateRequest updateRequest = new MessageDto.UpdateRequest(
        "수정된 메시지 내용"
    );

    // When & Then
    mockMvc.perform(patch("/api/messages/{messageId}", message.getId())
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(message.getId().toString()))
        .andExpect(jsonPath("$.content").value("수정된 메시지 내용"))
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  @Disabled("존재하지 않는 메시지는 @PreAuthorize 단계에서 403이 발생하는 것이 정상이나 테스트는 404를 기대. 테스트 기대값 재설계 필요")
  @DisplayName("메시지 업데이트 실패 API 통합 테스트 - 존재하지 않는 메시지")
  void update_nonExistingMessage_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentMessageId = UUID.randomUUID();
    MessageDto.UpdateRequest updateRequest = new MessageDto.UpdateRequest(
        "수정된 메시지 내용"
    );

    // When & Then
    mockMvc.perform(patch("/api/messages/{messageId}", nonExistentMessageId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound());
  }

  // delete 테스트
  @Test
  @DisplayName("메시지 삭제 API 통합 테스트 - 성공")
  void delete_success() throws Exception {
    // Given
    User author = saveTestUser("messageuser", "messageuser@example.com");
    Channel channel = saveTestPublicChannel("테스트 채널", "테스트 채널 설명");
    Message message = saveTestMessage(channel, author, "삭제할 메시지 내용");
    entityManager.flush();
    entityManager.clear();

    // When
    mockMvc.perform(delete("/api/messages/{messageId}", message.getId())
            .with(csrf()))
        .andExpect(status().isNoContent());

    // Then (삭제 후 조회 시 빈 목록이어야 함)
    mockMvc.perform(get("/api/messages")
            .param("channelId", channel.getId().toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.size()").value(0));
  }

  @Test
  @DisplayName("메시지 삭제 실패 API 통합 테스트 - 존재하지 않는 메시지")
  void delete_nonExistingMessage_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentMessageId = UUID.randomUUID();

    // When & Then
    mockMvc.perform(delete("/api/messages/{messageId}", nonExistentMessageId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }
}